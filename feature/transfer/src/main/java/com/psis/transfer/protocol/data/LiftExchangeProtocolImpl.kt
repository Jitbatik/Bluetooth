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
    override fun request(
        socket: BluetoothSocket,
        bytes: ByteArray
    ) {
        if (bytes.isEmpty()) return

        dataStreamHelpers.sendToStream(
            socket = socket,
            value = bytes + calculateCRC16(bytes).toByteArray()
        )
    }

    override fun sendCommand(socket: BluetoothSocket, command: ByteArray) {
        if (command.isEmpty()) return
        val packet = command + calculateCRC16(command).toByteArray()
        dataStreamHelpers.sendToStream(socket, packet)
    }


    override fun listen(socket: BluetoothSocket): Flow<List<Byte>> =
        dataStreamHelpers.readFromStream(socket)
            .mapNotNull { raw ->
                if (raw.isCRCValid()) raw.payload().toLittleEndian() else null
            }


    private fun calculateChecksum(packet: ByteArray): Int {
        val crc = calculateCRC16(packet.dropLast(2).toByteArray())
        return ((crc and 0xFF) shl 8) or (crc shr 8)
    }

    private fun ByteArray.isCRCValid(): Boolean {
        if (size < MIN_PACKET_SIZE) return false

        return this.toWord(size - 2) == calculateChecksum(this)
    }

    private fun ByteArray.payload(): List<Byte> = slice(3 until size - 2)

    private fun List<Byte>.toLittleEndian(): List<Byte> = buildList {
        val iterator = this@toLittleEndian.iterator()
        while (iterator.hasNext()) {
            val first = iterator.next()
            if (iterator.hasNext()) {
                val second = iterator.next()
                // little-endian → сначала старший, потом младший
                add(second)
                add(first)
            } else {
                // остался один байт
                add(first)
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
        private const val MIN_PACKET_SIZE = 10
        private const val CRC16_INITIAL = 0xFFFF
        private const val CRC16_POLYNOMIAL = 0xA001
    }
}