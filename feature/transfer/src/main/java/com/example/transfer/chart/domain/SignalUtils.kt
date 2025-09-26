package com.example.transfer.chart.domain

import com.example.transfer.protocol.domain.utils.ByteUtils.getBitFromByteList
import com.example.transfer.protocol.domain.utils.ByteUtils.toIntFromByteList

object SignalUtils {
    fun extractSignalValueFromByteData(byteData: List<Byte>, offset: Int, type: String): Int {

        val size = getSignalSize(offset, type)
        val slice = byteData.subList(offset, size)
        return when {
            type.matches(Regex("b[0-7]")) -> {
                val bitIndex = type.removePrefix("b").toInt()
                slice.getBitFromByteList(bitIndex)  // если это бит извлекаем его
            }

            else -> slice.toIntFromByteList() // если нет, то возвращаем целое
        }
    }

    fun getSignalSize(offset: Int, type: String): Int {
        return when (type) {
            "u32" -> offset + 4
            "u16" -> offset + 2
            "u8", "e8" -> offset + 1
            in BITS_TYPES -> offset + 1
            else -> offset + 1 // fallback для неизвестных типов
        }
    }

    private val BITS_TYPES = listOf("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7")
}