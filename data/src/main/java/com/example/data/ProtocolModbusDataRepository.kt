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
                charByte = byteValue.toByte(),
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

        scope.launch {
            Log.d(tag, "Processing incoming data")
            try {
                dataStreamRepository.readFromStream(socket = socket, canRead = canRead)
                    .cancellable()
                    .takeWhile { canRead.value }
                    .collect { byteArray ->
                        if (byteArray.size >= 14) {
                            testFunction(byteArray.mapToModbusPacket())
                            packetBuffer.add(byteArray.mapToModbusPacket())
                        }
                        if (packetBuffer.size == ProtocolModbusDataRepository.MAX_PACKET_SIZE) {
                            Log.d(tag, "Data packet assembled")
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

//        scope.launch {
//            while (canRead.value) {
//                try {
//                    requestMissingBluetoothPackets(packetBuffer = packetBuffer)
//                    //delay(4000)
//                } catch (e: Exception) {
//                    Log.e(tag, "Error in requesting data: ${e.message}")
//                    canRead.value = false
//                }
//            }
//        }
    }

    private fun processAndStorePackets(packetBuffer: List<ModbusPacket>) {
        packetBuffer.forEach { packet ->
            _bluetoothModbusPacketsFlow.update { currentList ->
                currentList.toMutableList().apply {
                    add(packet)
                }
            }
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
            dataList = slice(11 until this.size - 2).map { it.toUByte() },
            checksum = (this[this.size - 2].toUByte()
                .toInt() shl 8) or (this[this.size - 1].toUByte().toInt())

        )
    }

    private fun testFunction(packet: ModbusPacket) {
        Log.d(
            tag, "ModbusPacket(/n" +
                    "slaveAddress= ${packet.slaveAddress.toString(16)},\n" +
                    "functionCode= ${packet.functionCode.toString(16)},\n" +
                    "startRegisterRead= ${packet.startRegisterRead.toString(16)},\n" +
                    "quantityRegisterRead= ${packet.quantityRegisterRead.toString(16)},\n" +
                    "startRegisterWrite= ${packet.startRegisterWrite.toString(16)},\n" +
                    "quantityRegisterWrite= ${packet.quantityRegisterWrite.toString(16)},\n" +
                    "counter= ${packet.counter.toString(16)},\n" +
                    "dataList= [${packet.dataList.joinToString(", ") { it.toString(16) }}],\n" +
                    "checksum= ${packet.checksum.toString(16)}\n" +
                    ")"
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
        private const val MAX_PACKET_SIZE = 1
    }
}