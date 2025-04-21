package com.example.bluetooth.presentation

import com.example.transfer.model.ChartParameters
import com.example.transfer.model.LiftParameters

data class ParametersState(
    val parametersGroup: List<LiftParameters>,
    val chartParameters: ChartParameters,
    val onEvents: (ParametersIntent) -> Unit,
)
