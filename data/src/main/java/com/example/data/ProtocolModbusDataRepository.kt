package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.domain.model.CharData
import com.example.domain.model.ControllerConfig
import com.example.domain.model.KeyMode
import com.example.domain.model.ModbusPacket
import com.example.domain.model.Range
import com.example.domain.model.Rotate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Repository for processing data via
 * [BluetoothSocket] using the Modbus protocol.
 * */
class ProtocolModbusDataRepository @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
    private val dataStreamRepository: DataStreamRepository,
) {
    private val tag = ProtocolModbusDataRepository::class.java.simpleName

    private val _bluetoothModbusPacketsFlow = MutableStateFlow<List<ModbusPacket>>(emptyList())

    private val originalResponse = byteArrayOf(
        0x01.toByte(), 0x17.toByte(), 0x04.toByte(), 0x00.toByte(), 0x00.toByte(),
        0x00.toByte(), 0x00.toByte()
    )
    private var response = originalResponse.copyOf()


    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBluetoothDataFlow(): Flow<List<CharData>> {
        return observeSocketState()
            .onEach { socketState ->
                Log.d(tag, "Socket state is $socketState")
                if (socketState) processIncomingBluetoothData()
            }
            .flatMapLatest {
                _bluetoothModbusPacketsFlow
                    .filter { it.size == MAX_PACKET_SIZE }
                    .map { it.mapToListCharData() }
            }
    }

    //TODO("сделать код чище и удобнее")
    fun observeControllerConfigFlow() = flow {
        _bluetoothModbusPacketsFlow.collect { packets ->
            if (packets.size >= 2) {
                val mergedData = listOf(packets[0].startRegisterRead, packets[1].startRegisterRead)
                emit(processControllerConfig(mergedData))
            } else {
                emit(
                    ControllerConfig(
                        range = Range(startRow = 0, endRow = 0, startCol = 0, endCol = 0),
                    )
                )
            }
        }
    }


    private fun processControllerConfig(startRegisterRead: List<Int>): ControllerConfig {
        val highByteFirst = (startRegisterRead[0] shr 8) and 0xFF
        val lowByteFirst = startRegisterRead[0] and 0xFF

        val highByteTwo = (startRegisterRead[1] shr 8) and 0xFF
        val lowByteTwo = startRegisterRead[1] and 0xFF

        val adrrFirst = ((highByteFirst shl 8) or (lowByteFirst and 0xFF))
        val insFirst = (((adrrFirst shl 2) + 0x100) and 0x3f00) or ((adrrFirst + 1) and 0x3f)

        val adrrTwo = ((highByteTwo shl 8) or (lowByteTwo and 0xFF))
        val insTwo = (((adrrTwo shl 2) + 0x100) and 0x3f00) or ((adrrTwo + 1) and 0x3f)

        val rotate = if ((adrrFirst and 0x4000) != 0) Rotate.PORTRAIT else Rotate.LANDSCAPE
        val keyMode = when (adrrFirst and 0x3000) {
            0x0000 -> KeyMode.BASIC
            0x1000 -> KeyMode.NUMERIC
            0x2000 -> KeyMode.NONE
            else -> KeyMode.NONE
        }
        val range = Range(
            startRow = (insFirst shr 8) and 0xFF,
            endRow = (insTwo shr 8) and 0xFF,
            startCol = insFirst and 0xFF,
            endCol = insTwo and 0xFF,
        )

        val isBorder = true //TODO: потом сделать проверку для оптимизации
        return ControllerConfig(
            range = range,
            rotate = rotate,
            keyMode = keyMode,
            isBorder = isBorder
        )
    }


    private fun List<ModbusPacket>.mapToListCharData(): List<CharData> {
        return this.flatMap { packet ->
            val startTest = packet.dataList.take(120)
            val endTest = packet.dataList.drop(120).take(120)

            startTest.mapIndexed { index, byteValue ->
                val colorBackgroundByte = endTest.getOrElse(index) { 0 }

                CharData(
                    charByte = byteValue,
                    colorByte = (colorBackgroundByte.toInt() and 0xF),
                    backgroundByte = ((colorBackgroundByte.toInt() shr 4) and 0xF),
                )
            }
        }
    }


    private fun observeSocketState() =
        bluetoothSocketProvider.bluetoothSocket.map { it?.isConnected == true }


    private suspend fun processIncomingBluetoothData() = coroutineScope {
        val socket = getActiveBluetoothSocket() ?: return@coroutineScope
        val canRead = MutableStateFlow(true)
        val packetBuffer = mutableListOf<ModbusPacket>()

        launch(Dispatchers.IO) {
            Log.d(tag, "Processing incoming data")
            try {
                dataStreamRepository.readFromStream(socket, canRead)
                    .cancellable()
                    .takeWhile { canRead.value }
                    .collect { byteArray ->
                        handleIncomingData(
                            byteArray = byteArray,
                            packetBuffer = packetBuffer,
                            socket = socket
                        )
                    }
            } catch (e: Exception) {
                Log.e(tag, "Error in reading stream: ${e.message}")
            } finally {
                canRead.value = false
            }
        }
    }

    private fun handleIncomingData(
        byteArray: ByteArray,
        packetBuffer: MutableList<ModbusPacket>,
        socket: BluetoothSocket,
    ) {
        if (byteArray.size < 14) return
        val modbusPacket = byteArray.toModbusPacket() ?: return
        packetBuffer.add(modbusPacket)
        respondToPacket(socket)
        if (modbusPacket.startRegisterWrite == 54992) {
            processAndStorePackets(packetBuffer)
            packetBuffer.clear()
        }
    }

    private fun ByteArray.toModbusPacket(): ModbusPacket? {
        val packet = ModbusPacket(
            slaveAddress = this[0].toUByte().toInt(),
            functionCode = this[1].toUByte().toInt(),
            startRegisterRead = ((this[2].toUByte().toInt() shl 8) or this[3].toUByte().toInt()),
            quantityRegisterRead = ((this[4].toUByte().toInt() shl 8) or this[5].toUByte().toInt()),
            startRegisterWrite = ((this[6].toUByte().toInt() shl 8) or this[7].toUByte().toInt()),
            quantityRegisterWrite = ((this[8].toUByte().toInt() shl 8) or this[9].toUByte()
                .toInt()),
            counter = this[10].toUByte().toInt(),
            dataList = slice(11 until size - 2),
            checksum = (this[size - 2].toUByte().toInt() shl 8) or this[size - 1].toUByte().toInt()
        )
        return if (packet.checksum == calculateChecksum(this)) packet else null
    }

    private fun calculateChecksum(packet: ByteArray): Int {
        val crc = calculateCRC16(packet.dropLast(2).toByteArray())
        return ((crc and 0xFF) shl 8) or (crc shr 8)
    }

    private fun calculateCRC16(buf: ByteArray): Int {
        var crc = CRC16_INITIAL
        for (byte in buf) {
            crc = crc xor (byte.toInt() and 0xFF)
            repeat(8) {
                crc = if (crc and 0x0001 != 0) {
                    (crc shr 1) xor CRC16_POLYNOMIAL
                } else {
                    crc shr 1
                }
            }
        }
        return crc
    }

    //TODO: Убрать после тестов
    //
    private val _answerFlowTest = MutableStateFlow("Command send")
    fun getAnswerTest(): Flow<String> = _answerFlowTest
    //

    private fun respondToPacket(socket: BluetoothSocket) {
        val checksum = calculateCRC16(response)
        val answer = response + checksum.toByteArray()
        _answerFlowTest.value = answer.joinToString(" ") { "%02X".format(it) }
        Log.d(tag, "Respond: ${answer.joinToString(" ") { "%02X".format(it) }}")
        dataStreamRepository.sendToStream(socket = socket, value = answer)
        //response = originalResponse.copyOf()
    }

    private fun Int.toByteArray(): ByteArray =
        byteArrayOf((this and 0xFF).toByte(), (this shr 8).toByte())

    private fun processAndStorePackets(packetBuffer: List<ModbusPacket>) =
        packetBuffer.forEach { updateDataPacket(it) }

    private fun updateDataPacket(data: ModbusPacket) {
        _bluetoothModbusPacketsFlow.update { currentList ->
            currentList.toMutableList().apply {
                val existingIndex =
                    indexOfFirst { it.startRegisterWrite == data.startRegisterWrite }
                if (existingIndex != -1) this[existingIndex] = data else add(data)
                sortBy { it.startRegisterWrite }
            }
        }
    }

    private fun getActiveBluetoothSocket(): BluetoothSocket? {
        val socket = bluetoothSocketProvider.bluetoothSocket.value
        if (socket == null || !socket.isConnected) {
            val error = "Socket is ${if (socket == null) "null" else "not connected"}"
            Log.d(tag, error)
            return null
        }
        return socket
    }

    fun sendToStream(value: ByteArray) {
        val checksum = calculateCRC16(value)
        val unsignedValue = value + checksum.toByteArray()
        Log.d(tag, "Command: ${unsignedValue.joinToString(" ") { "%02X".format(it) }}")
        response = value
    }

    companion object {
        private const val MAX_PACKET_SIZE = 7
        private const val CRC16_POLYNOMIAL = 0xA001
        private const val CRC16_INITIAL = 0xFFFF
    }
}