package com.example.transfer.chart.domain.model

data class GraphSeries(
    val name: String,
    val points: List<DataPoint>,
    val color: SignalColor? = null
)