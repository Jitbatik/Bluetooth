package com.psis.transfer.chart.domain

import com.psis.transfer.protocol.domain.utils.ByteUtils.getBitFromByteList
import com.psis.transfer.protocol.domain.utils.ByteUtils.toIntFromByteList

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

    private fun getSignalSize(offset: Int, type: String): Int {
        return when (type) {
            "u32" -> offset + 4
            "u16" -> offset + 2
            "u8", "e8" -> offset + 1
            in BITS_TYPES -> offset + 1
            else -> offset + 1 // fallback для неизвестных типов
        }
    }

    fun updateSignalInByteData(
        byteData: List<Byte>,
        offset: Int,
        type: String,
        value: Int
    ): List<Byte> {
        // Создаем мутабельную копию
        val result = byteData.toMutableList()

        when (type) {
            "u32" -> {
                // Записываем 4 байта (little-endian)
                result[offset] = (value and 0xFF).toByte()
                result[offset + 1] = ((value shr 8) and 0xFF).toByte()
                result[offset + 2] = ((value shr 16) and 0xFF).toByte()
                result[offset + 3] = ((value shr 24) and 0xFF).toByte()
            }
            "u16" -> {
                // Записываем 2 байта (little-endian)
                result[offset] = (value and 0xFF).toByte()
                result[offset + 1] = ((value shr 8) and 0xFF).toByte()
            }
            "u8", "e8" -> {
                // Записываем 1 байт
                result[offset] = (value and 0xFF).toByte()
            }
            in BITS_TYPES -> {
                // Обновляем отдельный бит
                val bitIndex = type.removePrefix("b").toInt()
                val byteIndex = offset
                val currentByte = result[byteIndex].toInt() and 0xFF
                val bitMask = 1 shl bitIndex

                val newByte = if (value != 0) {
                    currentByte or bitMask
                } else {
                    currentByte and bitMask.inv()
                }

                result[byteIndex] = newByte.toByte()
            }
            else -> throw IllegalArgumentException("Unsupported signal type: $type")
        }

        return result
    }

    private val BITS_TYPES = listOf("b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7")
}