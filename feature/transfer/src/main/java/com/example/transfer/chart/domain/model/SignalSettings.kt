package com.example.transfer.chart.domain.model

data class SignalSettings(
    val id: String,
    val name: String,
    val start: Int,
    val end: Int,
    val isVisible: Boolean,
    val color: SignalColor
)