package com.example.bluetooth.model

data class SignalSettings(
    val id: String,
    val name: String,
    val isVisible: Boolean,
    val color: SignalColor
)

data class SignalColor(val red: Int, val green: Int, val blue: Int)

data class ChartSettings(
    val title: String,
    val description: String,
    val signals: List<SignalSettings>
)
