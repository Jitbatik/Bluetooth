package com.example.transfer.domain.utils

import com.example.transfer.model.ByteData

object ByteUtils {
    private fun bytesToLong(byteList: List<UByte>, isLittleEndian: Boolean = true): Long =
        (if (isLittleEndian) byteList.reversed() else byteList)
            .fold(0L) { acc, byte -> (acc shl 8) or byte.toLong() }


    private fun bytesToInt(byteList: List<UByte>, isLittleEndian: Boolean = true): Int =
        (if (isLittleEndian) byteList.reversed() else byteList)
            .fold(0L) { acc, byte -> (acc shl 8) or byte.toLong() }.toInt()


    private fun bytesToBits(byteList: List<UByte>, isLittleEndian: Boolean = true): List<Int> =
        (if (isLittleEndian) byteList.reversed() else byteList)
            .flatMap { byte -> List(8) { (byte.toInt() shr (7 - it)) and 1 } }

    fun List<ByteData>.toIntFromByteData() = bytesToInt(this.map { it.byte.toUByte() })
    fun List<ByteData>.toLongFromByteData() = bytesToLong(this.map { it.byte.toUByte() })
    fun List<ByteData>.toIntListFromByteData() = bytesToBits(this.map { it.byte.toUByte() })
    fun Byte.toIntUnsigned(): Int = toUByte().toInt()
}