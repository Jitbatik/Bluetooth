package com.example.transfer.chart.domain

import com.example.transfer.chart.domain.model.ChartSettings
import com.example.transfer.chart.domain.model.ChartSignalsConfig


object ChartSettingsDefaults {
    fun getDefault() =  ChartSettings(
        title = "",
        description = "",
        config = ChartSignalsConfig(
            timestampSignal = null,
            millisSignal = null,
            signals = emptyList()
        )
    )
}