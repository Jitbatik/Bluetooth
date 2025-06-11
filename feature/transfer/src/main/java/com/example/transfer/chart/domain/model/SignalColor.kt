package com.example.transfer.chart.domain.model

data class SignalColor(
    val red: Int,
    val green: Int,
    val blue: Int
)  {
    fun toHex(): String = "#%02X%02X%02X".format(red, green, blue)
}