package com.psis.elimlift.presentation.view.parameters.viewmodel

import com.psis.elimlift.presentation.view.parameters.model.ParameterDisplayData
import com.psis.transfer.chart.domain.model.ChartConfig

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