package com.example.bluetooth.presentation.view.parameters.viewmodel

import androidx.compose.ui.geometry.Offset
import com.example.bluetooth.presentation.view.parameters.model.Chart
import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import com.example.transfer.chart.domain.model.ChartConfig

data class ParametersState(
    val time: String,
    val chartData: List<Chart>,
    val tapPosition: Offset?,
    val popData: ParameterDisplayData,
    val chartConfig: ChartConfig,
)
