package com.example.transfer.chart.domain.model

data class ChartSettings(
    val title: String,
    val description: String,
    val signals: List<SignalSettings>
)