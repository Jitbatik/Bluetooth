package com.psis.transfer.chart.domain.model

data class DataPoint(
    val xCoordinate: Float,
    val yCoordinate: Float,
    val timestamp: Long,
    val timeMilliseconds: Int
)