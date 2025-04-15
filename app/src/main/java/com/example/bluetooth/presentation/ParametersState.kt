package com.example.bluetooth.presentation

import com.example.transfer.model.ChartParameters
import com.example.transfer.model.ParametersGroup

data class ParametersState(
    val parametersGroup: ParametersGroup,
    val chartParameters: Map<Int, ChartParameters>,
    val onEvents: (Int, ParametersIntent) -> Unit,
)
