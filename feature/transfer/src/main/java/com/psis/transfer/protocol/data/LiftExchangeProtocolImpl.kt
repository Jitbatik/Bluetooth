package com.psis.transfer.protocol.data

import android.bluetooth.BluetoothSocket
import com.psis.transfer.protocol.domain.ExchangeProtocol
import com.psis.transfer.protocol.domain.utils.ByteUtils.toIntUnsigned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class LiftExchangeProtocolImpl @Inject constructor(
    private val dataStreamHelpers: DataStreamHelpers,
) : ExchangeProtocol {
    override fun sendCommand(socket: BluetoothSocket, command: ByteArray) {
        if (command.isEmpty()) return
        val packet = command + calculateCRC16(command).toByteArray()
        dataStreamHelpers.sendToStream(socket, packet)
    }

    override fun listen(socket: BluetoothSocket): Flow<List<Byte>> =
        dataStreamHelpers.readFromStream(socket)
            .mapNotNull { packet ->
                packet.takeIf { it.isCRCValid() }
                    ?.toList()
                    ?.toLittleEndian(rangesToFlip = listOf(3..packet.size - 3))
            }


    private fun calculateChecksum(packet: ByteArray): Int {
        val crc = calculateCRC16(packet.dropLast(2).toByteArray())
        return ((crc and 0xFF) shl 8) or (crc shr 8)
    }

    private fun ByteArray.isCRCValid(): Boolean {
        if (size < MIN_PACKET_SIZE) return false

        return this.toWord(size - 2) == calculateChecksum(this)
    }


    private fun List<Byte>.toLittleEndian(
        rangesToFlip: List<IntRange>
    ): List<Byte> = buildList {
        val total = this@toLittleEndian.size

        var index = 0
        while (index < total) {
            val range = rangesToFlip.firstOrNull { index in it }

            if (range == null) {
                // этот байт вне диапазона — добавляем как есть
                add(this@toLittleEndian[index])
                index++
            } else {
                // находим конец диапазона
                val end = range.last.coerceAtMost(total - 1)
                val segment = this@toLittleEndian.subList(index, end + 1)

                // переворачиваем по парам
                val flipped = buildList {
                    val iter = segment.iterator()
                    while (iter.hasNext()) {
                        val first = iter.next()
                        if (iter.hasNext()) {
                            val second = iter.next()
                            add(second)
                            add(first)
                        } else {
                            add(first)
                        }
                    }
                }

                addAll(flipped)
                index = end + 1
            }
        }
    }


    private fun ByteArray.toWord(offset: Int): Int =
        ((this[offset].toIntUnsigned() shl 8) or this[offset + 1].toIntUnsigned())

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

    companion object {
        private val TAG = LiftExchangeProtocolImpl::class.java.simpleName
        private const val MIN_PACKET_SIZE = 10
        private const val CRC16_INITIAL = 0xFFFF
        private const val CRC16_POLYNOMIAL = 0xA001
    }
}