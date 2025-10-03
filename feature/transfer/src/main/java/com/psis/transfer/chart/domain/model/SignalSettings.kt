package com.psis.transfer.chart.domain.model

data class SignalSettings(
    val name: String,
    val comment: String,
    val offset: Int,
    val type: String,
    val codes: List<SignalCode> = emptyList(), // Только для e8
    val isVisible: Boolean = true,
    val color: SignalColor = SignalColor(0, 255, 0)
)

data class SignalCode(
    val value: Int,
    val description: String
)