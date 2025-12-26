package com.psis.transfer.chart.domain.model

data class Point(
    val time: Long,
    val millis: Int,
    val value: Int,
)

// Функция для получения времени конкретной точки
fun Point.timeMs(): Long = time * 1000L + millis