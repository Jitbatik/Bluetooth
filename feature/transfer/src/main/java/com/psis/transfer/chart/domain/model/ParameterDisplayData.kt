package com.psis.transfer.chart.domain.model

data class ParameterDisplayData(
    val selectedIndex: Int? = null,
    val timestamp: Long = 0L,
    val timeMilliseconds: Int = 0,
    val parameters: Map<String, DisplayValueWithColor> = emptyMap()
)