package com.example.bluetooth.presentation.view.parameters.model

data class ParameterDisplayData(
    val selectedIndex: Int?,
    val timestamp: Long,
    val timeMilliseconds: Int,
    val parameters: Map<String, DisplayValueWithColor>
)