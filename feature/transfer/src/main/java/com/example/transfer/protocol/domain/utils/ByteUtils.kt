package com.example.transfer.protocol.domain.utils

import com.example.transfer.protocol.domain.model.ByteData

object ByteUtils {
    private fun bytesToNumber(
        byteList: List<UByte>,
        isLittleEndian: Boolean = true
    ): Long {
        val ordered = if (isLittleEndian) byteList.reversed() else byteList
        return ordered.fold(0L) { acc, byte -> (acc shl 8) or byte.toLong() }
    }

    private fun bytesToInt(byteList: List<UByte>, isLittleEndian: Boolean = true): Int {
        require(byteList.size <= 4) { "Too many bytes for Int conversion: ${byteList.size}" }
        return bytesToNumber(byteList, isLittleEndian).toInt()
    }

    private fun bytesToLong(byteList: List<UByte>, isLittleEndian: Boolean = true): Long {
        require(byteList.size <= 8) { "Too many bytes for Long conversion: ${byteList.size}" }
        return bytesToNumber(byteList, isLittleEndian)
    }

    private fun getBitFromBytes(
        byteList: List<UByte>,
        bitIndex: Int,
        isLittleEndian: Boolean = true,
    ): Int {
        require(bitIndex >= 0) { "Bit index must be non-negative" }

        val ordered = if (isLittleEndian) byteList.reversed() else byteList
        val byteIndex = bitIndex / 8
        val bitInByte = bitIndex % 8

        if (byteIndex >= ordered.size) return 0
        return (ordered[byteIndex].toInt() shr bitInByte) and 1
    }

    private fun bytesToBitList(
        byteList: List<UByte>,
        isLittleEndian: Boolean = true,
        mostSignificantBitFirst: Boolean = true
    ): List<Int> {
        val ordered = if (isLittleEndian) byteList.reversed() else byteList
        return ordered.flatMap { byte ->
            val bits = List(8) { (byte.toInt() shr it) and 1 }
            if (mostSignificantBitFirst) bits.reversed() else bits
        }
    }

    fun List<ByteData>.toIntLE() = bytesToInt(map { it.byte.toUByte() })
    fun List<ByteData>.toLongLE() = bytesToLong(this.map { it.byte.toUByte() })
    fun List<ByteData>.toBitListLE() = bytesToBitList(this.map { it.byte.toUByte() })
    fun List<ByteData>.getBitLE(bitIndex: Int): Int =
        getBitFromBytes(map { it.byte.toUByte() }, bitIndex)

    fun Byte.toIntUnsigned(): Int = toUByte().toInt()
}