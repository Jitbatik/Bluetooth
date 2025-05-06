package com.example.transfer.data

import android.util.Log
import com.example.bluetooth.data.DataStreamRepository
import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.domain.utils.ByteUtils.toIntUnsigned
import com.example.transfer.model.ByteData
import com.example.transfer.model.ControllerConfig
import com.example.transfer.model.LiftPacket
import com.example.transfer.model.Range
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


// todo есть проблема с постоянным перезапросом и чтением потока нужно решить это в бизнес логике
class ProtocolLiftDataRepository @Inject constructor(
    private val dataStreamRepository: DataStreamRepository,
) : ProtocolDataRepository {
    private val _sharedDataFlow = MutableSharedFlow<List<ByteData>>()
    private var collectionJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _answerFlowTest = MutableStateFlow("Command send")
    override fun getAnswerTest(): Flow<String> = _answerFlowTest

    override fun observeData(command: ByteArray): Flow<List<ByteData>> {
        collectionJob?.cancel()
        collectionJob = scope.launch {
            startDataCollection(command).collect {
                _sharedDataFlow.emit(it)
            }
        }
        return _sharedDataFlow
    }

    private fun startDataCollection(command: ByteArray) = channelFlow {
        Log.d(
            Tag,
            "Starting data collection with command: ${command.joinToString()}"
        )
        val bufferMutex = Mutex()

        // read input
        val readJob = launch(Dispatchers.IO) {
            dataStreamRepository.observeSocketStream()
                .collect { byteArray ->
                    val dataPacket = byteArray.toLiftPacket()
                    Log.d(
                        Tag,
                        "RESPOND: ${byteArray.joinToString(" ") { "%02X".format(it) }}"
                    )
                    dataPacket?.let { bufferMutex.withLock { send(it.mapToListCharData()) } }
                }
        }

        // request
        val requestJob = launch(Dispatchers.IO) {
            while (isActive) {
                mutex.withLock {
                    if (command.isNotEmpty()) {
                        requestMissingBluetoothPackets(command)
                    }
                }
            }
        }

        awaitClose {
            Log.d(Tag, "Closing Bluetooth data flow")
            requestJob.cancel()
            readJob.cancel()
        }
    }

    override fun observeControllerConfig() = flow {
        emit(ControllerConfig(range = Range(startRow = 0, endRow = 0, startCol = 0, endCol = 0)))
    }

    override fun sendToStream(value: ByteArray) {
        val test = value + calculateCRC16(value).toByteArray()
        Log.d(Tag, "Send data: ${test.joinToString(" ") { "%02X".format(it) }}")
        dataStreamRepository.sendToStream(value = test)
    }

    private suspend fun requestMissingBluetoothPackets(command: ByteArray) {
        if (command.isEmpty()) return
        val test = command + calculateCRC16(command).toByteArray()
        Log.d(Tag, "REQUEST: ${test.joinToString(" ") { "%02X".format(it) }}")
        dataStreamRepository.sendToStream(value = test)
        delay(RETRY_DELAY_MS)
    }

    private fun Int.toByteArray(): ByteArray =
        byteArrayOf((this and 0xFF).toByte(), (this shr 8).toByte())

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


    private fun ByteArray.toLiftPacket(): LiftPacket? {
        if (size < MIN_PACKET_SIZE) return null
        val packet = LiftPacket(
            slaveAddress = this[0].toIntUnsigned(),
            functionCode = this[1].toIntUnsigned(),
            regSize = this[2].toIntUnsigned(),
            dataList = slice(3 until size - 2),
            checksum = this.toWord(size - 2)
        )
        return if (packet.checksum == calculateChecksum(this)) packet else null
    }

    private fun calculateChecksum(packet: ByteArray): Int {
        val crc = calculateCRC16(packet.dropLast(2).toByteArray())
        return ((crc and 0xFF) shl 8) or (crc shr 8)
    }

    private fun LiftPacket.mapToListCharData(): List<ByteData> {
        return dataList.chunked(2).flatMap { pair ->
            if (pair.size == 2) listOf(ByteData(byte = pair[1]), ByteData(byte = pair[0]))
            else listOf(ByteData(byte = pair[0]))
        }
    }

    private fun ByteArray.toWord(offset: Int): Int =
        ((this[offset].toIntUnsigned() shl 8) or this[offset + 1].toIntUnsigned())

    companion object {
        private const val MIN_PACKET_SIZE = 10
        private const val CRC16_INITIAL = 0xFFFF
        private const val CRC16_POLYNOMIAL = 0xA001
        private const val RETRY_DELAY_MS = 200L
        private val Tag = ProtocolLiftDataRepository::class.java.simpleName
    }
}