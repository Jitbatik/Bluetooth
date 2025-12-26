package com.psis.transfer.chart.domain.model

data class SignalUserSettings(
    val name: String,
    val isVisible: Boolean = true,
    val color: SignalColor = SignalColor(0, 255, 0)
)

data class SignalDefinition(
    val name: String,
    val comment: String,
    val offset: Int,
    val type: String,
    val codes: List<SignalCode> = emptyList()
)