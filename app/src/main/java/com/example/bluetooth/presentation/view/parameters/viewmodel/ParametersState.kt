package com.example.bluetooth.presentation.view.parameters.viewmodel

import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import com.example.transfer.chart.domain.model.ChartConfig

data class ParametersState(
    val time: String,
    val chartData: List<GraphSeries>,
    val popData: ParameterDisplayData,
    val chartConfig: ChartConfig,
)
