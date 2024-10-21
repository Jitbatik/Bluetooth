package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.domain.model.CharData
import com.example.domain.model.ModbusPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBluetoothDataFlow(): Flow<List<CharData>> {
        return observeSocketState()
            .onEach { socketState ->
                Log.d("SocketState", "Socket state is $socketState")
                if (socketState) processIncomingBluetoothData()
            }
            .flatMapLatest {
                _bluetoothModbusPacketsFlow
                    .filter { it.size == MAX_PACKET_SIZE }
                    .map { it.mapToListCharData() }
            }
    }

    private fun List<ModbusPacket>.mapToListCharData(): List<CharData> {
        return this.flatMap { it.dataList.take(it.quantityRegisterWrite) }.map { byteValue ->
            CharData(
                charByte = byteValue,
                colorByte = 1.toByte(),
                backgroundByte = 0.toByte()
            )
        }
    }

    private fun observeSocketState(): Flow<Boolean> {
        return bluetoothSocketProvider.bluetoothSocket
            .map { socket -> socket != null }
            .distinctUntilChanged()
    }

    private suspend fun processIncomingBluetoothData() {
        val socket = getActiveBluetoothSocket() ?: return
        val canRead = MutableStateFlow(true)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        val packetBuffer = mutableListOf<ModbusPacket>()
        val response =
            byteArrayOf(
                0x01.toByte(),
                0x17.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte()
                //crc
            )

        scope.launch {
            Log.d(tag, "Processing incoming data")
            try {
                dataStreamRepository.readFromStream(socket = socket, canRead = canRead)
                    .cancellable()
                    .takeWhile { canRead.value }
                    .collect { byteArray ->
                        if (byteArray.size < 14) return@collect

                        val modbusPacket = byteArray.mapToModbusPacket()

                        if (calculateChecksum(packet = byteArray) != modbusPacket.checksum) {
                            Log.d(tag, "Invalid checksum!")
                            return@collect
                        }
                        packetBuffer.add(modbusPacket)
                        answerTest(socket = socket, values = response)

                        if (modbusPacket.startRegisterWrite == 54992) {
                            Log.d(tag, "Data packet assemble: ${packetBuffer.count()}")
                            processAndStorePackets(packetBuffer)
                            packetBuffer.clear()
                        }
                    }
            } catch (e: Exception) {
                Log.e(tag, "Error in reading stream: ${e.message}")
            } finally {
                canRead.value = false
            }
        }
    }

    private fun calculateChecksum(packet: ByteArray): Int {
        val calculatedChecksum = calculateCRC16(packet.dropLast(2).toByteArray())
        return ((calculatedChecksum and 0xFF) shl 8) or (calculatedChecksum shr 8)
    }

    private fun calculateCRC16(buf: ByteArray): Int {
        var crc = 0xFFFF
        for (byte in buf) {
            crc = crc xor (byte.toInt() and 0xFF)
            repeat(8) {
                crc = if (crc and 0x0001 != 0) {
                    (crc shr 1) xor 0xA001
                } else {
                    crc shr 1
                }
            }
        }
        return crc
    }

    private fun answerTest(socket: BluetoothSocket, values: ByteArray) {
        val checksum = calculateChecksum(values)
        val checksumBytes = byteArrayOf(
            (checksum shr 8).toByte(),
            (checksum and 0xFF).toByte()
        )
        dataStreamRepository.sendToStream(socket = socket, value = values + checksumBytes)
    }

    private fun processAndStorePackets(packetBuffer: List<ModbusPacket>) =
        packetBuffer.forEach { updateDataPacket(it) }


    private fun updateDataPacket(data: ModbusPacket) {
        _bluetoothModbusPacketsFlow.update { currentList ->
            val mutableList = currentList.toMutableList()

            val existingIndex =
                mutableList.indexOfFirst { it.startRegisterWrite == data.startRegisterWrite }

            if (existingIndex != -1) {
                val existingData = mutableList[existingIndex]
                if (existingData != data) {
                    mutableList[existingIndex] = data
                }
            } else mutableList.add(data)

            mutableList.sortBy { it.startRegisterWrite }
            mutableList
        }
    }


    private fun ByteArray.mapToModbusPacket(): ModbusPacket {
        return ModbusPacket(
            slaveAddress = this[0].toUByte().toInt(),
            functionCode = this[1].toUByte().toInt(),
            startRegisterRead = ((this[2].toUByte().toInt() shl 8) or (this[3].toUByte().toInt())),
            quantityRegisterRead = ((this[4].toUByte().toInt() shl 8) or (this[5].toUByte()
                .toInt())),
            startRegisterWrite = ((this[6].toUByte().toInt() shl 8) or (this[7].toUByte().toInt())),
            quantityRegisterWrite = ((this[8].toUByte().toInt() shl 8) or (this[9].toUByte()
                .toInt())),
            counter = this[10].toUByte().toInt(),
            dataList = slice(11 until this.size - 2),
            checksum = (this[this.size - 2].toUByte()
                .toInt() shl 8) or (this[this.size - 1].toUByte().toInt())

        )
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
        val socket = getActiveBluetoothSocket() ?: return
        dataStreamRepository.sendToStream(socket = socket, value = value)
    }

    companion object {
        private const val MAX_PACKET_SIZE = 7
    }

//testFunction(modbusPacket)
//    private fun testFunction(packet: ModbusPacket) {
//        Log.d(
//            tag, "ModbusPacket(/n" +
//                    "slaveAddress= ${packet.slaveAddress.toString(16)},\n" +
//                    "functionCode= ${packet.functionCode.toString(16)},\n" +
//                    "startRegisterRead= ${packet.startRegisterRead.toString(16)},\n" +
//                    "quantityRegisterRead= ${packet.quantityRegisterRead.toString(16)},\n" +
//                    "startRegisterWrite= ${packet.startRegisterWrite.toString(16)},\n" +
//                    "quantityRegisterWrite= ${packet.quantityRegisterWrite.toString(16)},\n" +
//                    "counter= ${packet.counter.toString(16)},\n" +
//                    "dataList= [${packet.dataList.joinToString(", ") { it.toString(16) }}],\n" +
//                    "checksum= ${packet.checksum.toString(16)}\n" +
//                    ")"
//        )
//    }
}