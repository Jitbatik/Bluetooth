package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.domain.model.CharData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
 * [BluetoothSocket] using the UART protocol.
 * */
class ProtocolDataRepository @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
    private val dataStreamRepository: DataStreamRepository,
) {
    private val tag = this::class.java.simpleName
    private val _bluetoothDataPacketsFlow = MutableStateFlow<List<DataPacket>>(emptyList())

    /**
     * Subscribe data from Bluetooth.
     * */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeBluetoothDataFlow(): Flow<List<CharData>> {
        return observeSocketState()
            .onEach { socketState ->
                Log.d("SocketState", "Socket state is $socketState")
                if (socketState) processIncomingBluetoothData()
            }
            .flatMapLatest {
                _bluetoothDataPacketsFlow
                    .filter { it.size == MAX_PACKET_SIZE }
                    .map { it.mapToListCharData() }
            }
    }

    /**
     * Observe the [BluetoothSocket] connection status.
     * */
    private fun observeSocketState(): Flow<Boolean> {
        return bluetoothSocketProvider.bluetoothSocket
            .map { socket -> socket != null }
            .distinctUntilChanged()
    }

    /**
     * Mapping list [DataPacket] to list [CharData].
     * */
    private fun List<DataPacket>.mapToListCharData(): List<CharData> {
        return this.flatMap { it.dataBytes }.map {
            CharData(charByte = it, colorByte = 1.toByte(), backgroundByte = 0.toByte())
        }
    }

    /**
     * Process incoming data from [BluetoothSocket].
     * */
    private suspend fun processIncomingBluetoothData() {
        val socket = getActiveBluetoothSocket() ?: return
        val canRead = MutableStateFlow(true)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        val packetBuffer = mutableListOf<DataPacket>()

        scope.launch {
            Log.d(tag, "Processing incoming data")
            try {
                dataStreamRepository.readFromStream(socket = socket, canRead = canRead)
                    .cancellable()
                    .takeWhile { canRead.value }
                    .collect {
                        packetBuffer.add(it.mapToDataPacket())
                        if (packetBuffer.size == MAX_PACKET_SIZE) {
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

        scope.launch {
            while (canRead.value) {
                try {
                    requestMissingBluetoothPackets(socket = socket, packetBuffer = packetBuffer)
                    delay(5000)
                } catch (e: Exception) {
                    Log.e(tag, "Error in requesting data: ${e.message}")
                    canRead.value = false
                }
            }
        }
    }

    /**
     * Get the active [BluetoothSocket] if there is one.
     * */
    private fun getActiveBluetoothSocket(): BluetoothSocket? {
        val socket = bluetoothSocketProvider.bluetoothSocket.value
        if (socket == null || !socket.isConnected) {
            val error = "Socket is ${if (socket == null) "null" else "not connected"}"
            Log.d("ttt", error)
            return null
        }
        return socket
    }

    /**
     * Mapping a [ByteArray] to a [DataPacket].
     * */
    private fun ByteArray.mapToDataPacket(): DataPacket {
        val index = this[5].toInt()
        val listByte = this.drop(6)
        return DataPacket(index = index, dataBytes = listByte)
    }

    /**
     * Process and store packets in the [packetBuffer].
     * */
    private fun processAndStorePackets(packetBuffer: List<DataPacket>) =
        packetBuffer.forEach { updateDataPacket(it) }

    /**
     * Add or update a data packet in the [_bluetoothDataPacketsFlow].
     * Checks data for overwriting.
     * */
    private fun updateDataPacket(data: DataPacket) {
        _bluetoothDataPacketsFlow.update { currentList ->
            val mutableList = currentList.toMutableList()

            val existingIndex = mutableList.indexOfFirst { it.index == data.index }

            if (existingIndex != -1) {
                val existingData = mutableList[existingIndex]
                if (existingData != data) {
                    mutableList[existingIndex] = data
                }
            } else {
                mutableList.add(data)
            }
            mutableList
        }
    }

    /**
     *  Request missing Bluetooth  packets in [packetBuffer].
     * */
    private suspend fun requestMissingBluetoothPackets(
        socket: BluetoothSocket,
        packetBuffer: List<DataPacket>,
    ) {
        val missingIndices = findMissingPacketIndices(packetBuffer)

        missingIndices.forEach { missingIndex ->
            Log.d(tag, "Missing index: $missingIndex")

            val command = byteArrayOf(
                0xFE.toByte(),
                0x08.toByte(),
                0x00,
                0x00,
                0x00,
                missingIndex.toByte(),
                0x00,
                0x00,
                0x00,
                0x00
            )

            dataStreamRepository.sendToStream(socket = socket, value = command)
            delay(RETRY_DELAY_MS)
        }

        if (missingIndices.isEmpty()) {
            Log.d(tag, "No missing indices")
        }
    }

    /**
     *  Find the  missing packet indices.
     * */
    private fun findMissingPacketIndices(packetBuffer: List<DataPacket>): List<Int> {
        val requiredIndices = (0 until MAX_PACKET_SIZE).toSet()
        val presentIndices = packetBuffer.map { it.index }.toSet()

        return requiredIndices.subtract(presentIndices).toList()
    }

    fun sendToStream(value: ByteArray) {
        val socket = getActiveBluetoothSocket() ?: return
        dataStreamRepository.sendToStream(socket = socket, value = value)
    }

    companion object {
        private const val MAX_PACKET_SIZE = 20
        private const val RETRY_DELAY_MS = 100L
    }
}