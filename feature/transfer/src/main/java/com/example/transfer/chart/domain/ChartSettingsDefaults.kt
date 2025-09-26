package com.example.transfer.chart.domain

import com.example.transfer.chart.domain.model.ChartSettings
import com.example.transfer.chart.domain.model.ChartSignalsConfig


object ChartSettingsDefaults {
    fun getDefault() =  ChartSettings(
        title = "Отображение графика",
        description = "Нет данных о состояниях лифта",
        config = ChartSignalsConfig(
            timestampSignal = null,
            millisSignal = null,
            signals = emptyList()
        )
    )
}