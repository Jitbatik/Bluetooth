package com.example.transfer

import android.util.Log
import com.example.domain.model.CharData
import com.example.domain.model.ControllerConfig
import com.example.domain.model.Range
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


class ProtocolUARTDataRepository @Inject constructor(
    private val dataStreamRepository: DataStreamRepository,
) {
    private val tag = ProtocolUARTDataRepository::class.java.simpleName

    fun observeBluetoothDataFlow(): Flow<List<CharData>> = channelFlow {
        Log.d(tag, "Initializing Bluetooth data flow")
        val packetBuffer = mutableListOf<UARTPacket>()
        val canRead = AtomicBoolean(true) //false
        val bufferMutex = Mutex()

        launch {
            while (isActive && canRead.get()) {
                requestMissingBluetoothPackets(packetBuffer = packetBuffer)
            }
        }

        launch {
            dataStreamRepository.observeSocketStream()
                .collect { byteArray ->
                    val dataPacket = byteArray.mapToDataPacket()
                    Log.d(tag, "Data: ${byteArray.joinToString(" ")}")
                    bufferMutex.withLock {
                        packetBuffer.add(dataPacket)
                        if (packetBuffer.size == MAX_PACKET_SIZE) {
                            Log.d(tag, "Data packet assembled")
                            send(packetBuffer.mapToListCharData())
                            packetBuffer.clear()
                        }
                    }
                }
        }
    }

    private suspend fun requestMissingBluetoothPackets(packetBuffer: List<UARTPacket>) {
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

            dataStreamRepository.sendToStream(value = command)
            delay(RETRY_DELAY_MS)
        }

        if (missingIndices.isEmpty()) {
            Log.d(tag, "No missing indices")
        }
    }

    private fun List<UARTPacket>.mapToListCharData(): List<CharData> {
        return this.sortedBy  { it.index }
            .flatMap { it.dataBytes }
            .map {
                CharData(
                    charByte = it,
                    colorByte = 0,
                    backgroundByte = 15,
                )
            }
    }

    private fun ByteArray.mapToDataPacket(): UARTPacket {
        val index = this[5].toInt()
        val listByte = this.drop(6)
        return UARTPacket(index = index, dataBytes = listByte)
    }

    fun observeControllerConfigFlow() = flow {
        emit(
            ControllerConfig(
                range = Range(startRow = 0, endRow = 0, startCol = 0, endCol = 0),
            )
        )
    }

    private val _answerFlowTest = MutableStateFlow("Command send")
    fun getAnswerTest(): Flow<String> = _answerFlowTest

    private fun findMissingPacketIndices(packetBuffer: List<UARTPacket>): List<Int> {
        val requiredIndices = (0 until MAX_PACKET_SIZE).toSet()
        val presentIndices = packetBuffer.map { it.index }.toSet()

        return requiredIndices.subtract(presentIndices).toList()
    }

    fun sendToStream(value: ByteArray) {
        dataStreamRepository.sendToStream(value = value)
    }

    companion object {
        private const val MAX_PACKET_SIZE = 20
        private const val RETRY_DELAY_MS = 100L
    }
}