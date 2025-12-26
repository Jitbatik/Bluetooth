package com.psis.transfer.chart.domain.model

data class Line(
    val name: String,
    val description: String,
    val points: List<Point>,
//    val color: SignalColor,
    // тип линии и тд
)