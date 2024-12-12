package com.example.transfer

import android.util.Log
import com.example.domain.model.CharData
import com.example.domain.model.ControllerConfig
import com.example.domain.model.Range
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProtocolModbusDataRepository @Inject constructor(
    private val dataStreamRepository: DataStreamRepository,
) {
    private val tag = ProtocolUARTDataRepository::class.java.simpleName

    fun observeBluetoothDataFlow(): Flow<List<CharData>> = flow {
        Log.d(tag, "Initializing Bluetooth data flow")
        val packetBuffer = mutableListOf<UARTPacket>()
        dataStreamRepository.observeSocketStream()
            .map { byteArray ->
                val dataPacket = byteArray.mapToDataPacket()
                Log.d(tag, "Data: ${byteArray.joinToString(" ")}")
                listOf(dataPacket).mapToListCharData()
            }
            .collect { charDataList -> emit(charDataList) }
    }.flowOn(Dispatchers.IO)

    private fun List<UARTPacket>.mapToListCharData(): List<CharData> {
        return this.sortedBy { it.index }
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