package com.example.transfer.data

import android.util.Log
import com.example.bluetooth.data.DataStreamRepository
import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.model.CharData
import com.example.transfer.model.ControllerConfig
import com.example.transfer.model.LiftPacket
import com.example.transfer.model.Range
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


// todo доп по запросам
// todo Чтение данных с мезанина начиная по адресу и столько-то регистров
// todo 01 03 00 00 00 20 44 12 [Адресс устройства] [код функции] 3х[Адресс начала чтения] [кол-во регистров] [контрСумма]
// todo Чтение даты/времени с мезанина для синхронизации
// todo 01 10 00 24 00 03 06 72 D7 67 69 00 8A 96 30
// todo Чтение Идентификатора Процессора
// todo 01 03 01 00 00 06 C4 34
class ProtocolLiftDataRepository @Inject constructor(
    private val dataStreamRepository: DataStreamRepository,
) : ProtocolDataRepository {


    //TODO: Убрать после тестов
    private val _answerFlowTest = MutableStateFlow("Command send")
    override fun getAnswerTest(): Flow<String> = _answerFlowTest
    //

    override fun observeData(): Flow<List<CharData>> = channelFlow {
        Log.d(Tag, "Initializing Bluetooth data flow")
        val bufferMutex = Mutex()

        launch {
            dataStreamRepository.observeSocketStream()
                .collect { byteArray ->
                    val dataPacket = byteArray.toLiftPacket()
                    Log.d(Tag, "Data: ${byteArray.joinToString(" ")}")

                    dataPacket?.let {
                        bufferMutex.withLock {
                            val test = it.mapToListCharData()
//                            Log.d(Tag, "Test: ${test}")
                            send(test)
                        }
                    }
                }
        }

        launch {
            dataStreamRepository.bluetoothSocketFlow
                .collectLatest { socket ->
                    if (socket != null) {
                        while (isActive) requestMissingBluetoothPackets()
                    }
                }
        }
    }.flowOn(Dispatchers.IO)


    override fun observeControllerConfig() = flow {
        emit(ControllerConfig(range = Range(startRow = 0, endRow = 0, startCol = 0, endCol = 0)))
    }

    override fun sendToStream(value: ByteArray) {
        dataStreamRepository.sendToStream(value = value)
    }
    //todo азобраться почему не катит 01 03 00 80 00 20 44 12
    private suspend fun requestMissingBluetoothPackets() {
        val command = byteArrayOf(0x01, 0x03, 0x00.toByte(), 0x80.toByte(), 0x00.toByte(), 0x20.toByte())
        val test = command + calculateCRC16(command).toByteArray()
        Log.d(Tag, "Send2: ${test.joinToString(" ") { "%02X".format(it) }}")
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
//        if (size < MIN_PACKET_SIZE) return null
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

    private fun LiftPacket.mapToListCharData(): List<CharData> {
        return dataList.flatMap { reg ->
            calculateTest(reg.toInt()).map { byte ->
                CharData(charByte = byte.toByte())
            }
        }
    }

    private fun calculateTest(value: Int): List<Int> = listOf((value shr 8) and 0xFF, value and 0xFF)

    private fun Byte.toIntUnsigned(): Int = toUByte().toInt()
    private fun ByteArray.toWord(offset: Int): Int =
        ((this[offset].toIntUnsigned() shl 8) or this[offset + 1].toIntUnsigned())

    companion object {
        private const val CRC16_INITIAL = 0xFFFF
        private const val CRC16_POLYNOMIAL = 0xA001
        private const val RETRY_DELAY_MS = 100L
        private val Tag = ProtocolLiftDataRepository::class.java.simpleName
    }
}