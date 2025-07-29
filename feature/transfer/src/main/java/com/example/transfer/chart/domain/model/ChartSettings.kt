package com.example.transfer.chart.domain.model

data class ChartSettings(
    val title: String,
    val description: String,
    val config: ChartSignalsConfig
)

data class ChartSignalsConfig(
    val timestampSignal: SignalSettings?,
    val millisSignal: SignalSettings?,
    val signals: List<SignalSettings>
)
