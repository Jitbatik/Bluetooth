package com.example.transfer.data

import android.util.Log
import com.example.bluetooth.data.DataStreamRepository
import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.model.ByteData
import com.example.transfer.model.ControllerConfig
import com.example.transfer.model.KeyMode
import com.example.transfer.model.PultPacket
import com.example.transfer.model.Range
import com.example.transfer.model.Rotate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ProtocolPultDataRepository @Inject constructor(
    private val dataStreamRepository: DataStreamRepository,
) : ProtocolDataRepository {
    private val tag = ProtocolPultDataRepository::class.java.simpleName
    private var response = ORIGINAL_RESPONSE.copyOf()
    private val _bluetoothModbusPacketsConfigFlow = MutableStateFlow(ControllerConfig())

    private val _answerFlowTest = MutableStateFlow("Command send")
    override fun getAnswerTest(): Flow<String> = _answerFlowTest

    override fun observeData(command: ByteArray): Flow<List<ByteData>> = flow {
        Log.d(tag, "Initializing Bluetooth data flow")
        val packetBuffer = mutableListOf<PultPacket>()

        dataStreamRepository.observeSocketStream()
            .collect { byteArray ->
                Log.d(tag, "Data: ${byteArray.joinToString(" ")}")

                if (packetBuffer.size >= 2) updateControllerConfig(
                    packetBuffer.take(2).map { it.startRegisterRead }
                )

                val result = processPacketBuffer(packetBuffer = packetBuffer, byteArray = byteArray)
                if (result.isNotEmpty()) emit(result)
            }
    }.flowOn(Dispatchers.IO)

    override fun observeControllerConfig(): StateFlow<ControllerConfig> =
        _bluetoothModbusPacketsConfigFlow

    override fun sendToStream(value: ByteArray) {
        response = value
    }

    private fun updateControllerConfig(startRegisterRead: List<Int>) {
        if (startRegisterRead.size < 2) return

        val addresses = startRegisterRead.map { calculateAddress(it) }
        val instructions = addresses.map { ((it shl 2) + 0x100) and 0x3f00 or ((it + 1) and 0x3f) }

        val rotate = if (addresses[0] and 0x4000 != 0) Rotate.PORTRAIT else Rotate.LANDSCAPE
        val keyMode = when (addresses[0] and 0x3000) {
            0x0000 -> KeyMode.BASIC
            0x1000 -> KeyMode.NUMERIC
            0x2000 -> KeyMode.NONE
            else -> KeyMode.NONE
        }

        val range = Range(
            startRow = (instructions[0] shr 8) and 0xFF,
            endRow = (instructions[1] shr 8) and 0xFF,
            startCol = instructions[0] and 0xFF,
            endCol = instructions[1] and 0xFF
        )

        _bluetoothModbusPacketsConfigFlow.update {
            it.copy(range = range, rotate = rotate, keyMode = keyMode, isBorder = true)
        }
    }


    private fun processPacketBuffer(
        packetBuffer: MutableList<PultPacket>,
        byteArray: ByteArray
    ): List<ByteData> {
        val modbusPacket = byteArray.toModbusPacket() ?: return emptyList()

        val existingIndex =
            packetBuffer.indexOfFirst { it.startRegisterWrite == modbusPacket.startRegisterWrite }
        if (existingIndex >= 0) packetBuffer[existingIndex] = modbusPacket
        else packetBuffer.add(modbusPacket)

        replyRespond()

        return if (modbusPacket.isFinalPacket() && packetBuffer.size == PACKET_COUNT) {
            val result = packetBuffer.sortedBy { it.startRegisterWrite }.mapToListCharData()
            packetBuffer.clear()
            result
        } else {
            emptyList()
        }
    }

    private fun replyRespond() {
        val answer = prepareResponse(response)
        _answerFlowTest.value = answer.joinToString(" ") { "%02X".format(it) }
        Log.d(tag, "Respond: ${_answerFlowTest.value}")
        dataStreamRepository.sendToStream(answer)
    }


    private fun ByteArray.toModbusPacket(): PultPacket? {
        if (size < MIN_PACKET_SIZE) return null
        val packet = PultPacket(
            slaveAddress = this[0].toIntUnsigned(),
            functionCode = this[1].toIntUnsigned(),
            startRegisterRead = this.toWord(2),
            quantityRegisterRead = this.toWord(4),
            startRegisterWrite = this.toWord(6),
            quantityRegisterWrite = this.toWord(8),
            counter = this[10].toIntUnsigned(),
            dataList = slice(11 until size - 2),
            checksum = this.toWord(size - 2)
        )
        return if (packet.checksum == calculateChecksum(this)) packet else null
    }

    private fun List<PultPacket>.mapToListCharData(): List<ByteData> = flatMap { packet ->
        val charBytes = packet.dataList.take(120)
        val colorBytes = packet.dataList.drop(120).take(120)

        charBytes.mapIndexed { index, charByte ->
            val colorBackgroundByte = colorBytes.getOrElse(index) { 0 }
            ByteData(
                byte = charByte,
                colorByte = (colorBackgroundByte.toInt() and 0xF),
                backgroundByte = ((colorBackgroundByte.toInt() shr 4) and 0xF),
            )
        }
    }

    private fun prepareResponse(value: ByteArray): ByteArray =
        value + calculateCRC16(value).toByteArray()

    private fun calculateAddress(value: Int): Int {
        val highByte = (value shr 8) and 0xFF
        val lowByte = value and 0xFF
        return (highByte shl 8) or lowByte
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

    private fun Byte.toIntUnsigned(): Int = toUByte().toInt()
    private fun ByteArray.toWord(offset: Int): Int =
        ((this[offset].toIntUnsigned() shl 8) or this[offset + 1].toIntUnsigned())

    private fun Int.toByteArray(): ByteArray =
        byteArrayOf((this and 0xFF).toByte(), (this shr 8).toByte())

    private fun PultPacket.isFinalPacket() = startRegisterWrite == FINAL_PACKET_REGISTER

    companion object {
        private val ORIGINAL_RESPONSE = byteArrayOf(0x01, 0x17, 0x04, 0x00, 0x00, 0x00, 0x00)
        private const val PACKET_COUNT = 7
        private const val FINAL_PACKET_REGISTER = 54992
        private const val MIN_PACKET_SIZE = 14
        private const val CRC16_POLYNOMIAL = 0xA001
        private const val CRC16_INITIAL = 0xFFFF
    }
}