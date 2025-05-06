package com.example.bluetooth.presentation

import com.example.transfer.model.ChartConfig
import com.example.transfer.model.LiftParameters

data class ParametersState(
    val parametersGroup: List<LiftParameters>,
    val chartConfig: ChartConfig,
    val onEvents: (ParametersIntent) -> Unit,
)
