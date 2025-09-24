package com.example.transfer.protocol.domain.utils

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

    fun Byte.toIntUnsigned(): Int = toUByte().toInt()

    fun List<Byte>.toIntFromByteList() = bytesToInt(map { it.toUByte() })
    fun List<Byte>.getBitFromByteList(bitIndex: Int): Int =
        getBitFromBytes(map { it.toUByte() }, bitIndex)
}