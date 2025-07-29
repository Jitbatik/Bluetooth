package com.example.bluetooth.presentation.view.parameters.viewmodel

import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import com.example.transfer.chart.domain.model.ChartConfig

object ParametersStateDefaults {
    fun getDefault() = ParametersState(
        time = "",
        chartData = emptyList(),
        popData = ParameterDisplayData(
            selectedIndex = null,
            timestamp = 0L,
            timeMilliseconds = 0,
            parameters = emptyMap()
        ),
        chartConfig = ChartConfig(),
        tapPosition = null,
    )
}