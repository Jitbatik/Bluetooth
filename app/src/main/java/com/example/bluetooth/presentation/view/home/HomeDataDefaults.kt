package com.example.bluetooth.presentation.view.home

import androidx.compose.ui.graphics.Color


object HomeDataDefaults {
    fun getDefault(): List<DataUI> {
        val byteArray = byteArrayOf(
            0x80.toByte(),
            0x82.toByte(),
            0x84.toByte(),
            0x85.toByte(),
            0x86.toByte(),
            0x87.toByte(),
            0x27.toByte(),
            0x32.toByte(),
            0xCE.toByte(),
            0x95.toByte(),
            0xDE.toByte(),
            0xA0.toByte(),
            0xE0.toByte(),
        )
        val resultArray = ByteArray(280)
        for (i in resultArray.indices) {
            resultArray[i] = byteArray[i % byteArray.size]
        }

        return resultArray.map { byte ->
            DataUI(
                data = String(byteArrayOf(byte), Charsets.ISO_8859_1)[0].toString(),
                color = Color.Black,
                background = getRandomColor(),
            )
        }
    }

    private fun getRandomColor(): Color {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color(r, g, b)
    }
}