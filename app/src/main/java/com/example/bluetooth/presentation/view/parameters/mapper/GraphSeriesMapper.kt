package com.example.bluetooth.presentation.view.parameters.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.example.bluetooth.presentation.view.parameters.model.Chart
import com.example.bluetooth.presentation.view.parameters.util.toUiColor
import com.example.transfer.chart.domain.model.GraphSeries as DomainGraphSeries

fun DomainGraphSeries.toUiChart(
    stepX: Float,
    stepY: Float,
    height: Float
): Chart {
    val points = this.points
    val minValue = points.minOfOrNull { it.yCoordinate } ?: 0f
    val maxValue = points.maxOfOrNull { it.yCoordinate } ?: 0f

    return Chart(
        name = this.name,
        points = this.points.map { (x, y) ->
            Offset(
                x = x * stepX,
                y = height - y * stepY // переворот оси Y
            )
        },
        color = this.color?.toUiColor() ?: Color.Black,
        minValue = minValue,
        maxValue = maxValue
    )
}

fun List<DomainGraphSeries>.mapToUiChartList(
    canvas: IntSize,
    stepCounterXAxis: Int,
    stepCountYAxis: Int
): List<Chart> {
    val stepX = canvas.width / stepCounterXAxis.toFloat()
    val stepY = canvas.height / stepCountYAxis.toFloat()

    return this.map { it.toUiChart(stepX, stepY, canvas.height.toFloat()) }
}


fun List<DomainGraphSeries>.maxY(): Float {
    return this.flatMap { it.points.map { point -> point.yCoordinate } }.maxOrNull() ?: 0f
}

fun List<DomainGraphSeries>.minY(): Float {
    return this.flatMap { it.points.map { point -> point.yCoordinate } }.minOrNull() ?: 0f
}