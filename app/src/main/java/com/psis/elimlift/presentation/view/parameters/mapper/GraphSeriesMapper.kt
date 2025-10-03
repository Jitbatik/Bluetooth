package com.psis.elimlift.presentation.view.parameters.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.psis.elimlift.presentation.view.parameters.model.Chart
import com.psis.elimlift.presentation.view.parameters.util.toUiColor
import kotlin.math.max
import com.psis.transfer.chart.domain.model.GraphSeries as DomainGraphSeries

fun DomainGraphSeries.toUiChart(
    canvas: IntSize,
    stepCounterXAxis: Int
): Chart {
    val points = this.points
    val minY =  0f
    val maxY = points.maxOfOrNull { it.yCoordinate } ?: 1f
    val yRange = max(1f, maxY - minY)

    val stepX = canvas.width / stepCounterXAxis.toFloat()
    val stepY = canvas.height / yRange

    return Chart(
        name = this.name,
        points = this.points.map { (x, y) ->
            Offset(
                x = x * stepX,
                y = canvas.height - ((y - minY) * stepY) // нормализация внутри своего диапазона
            )
        },
        color = this.color?.toUiColor() ?: Color.Black,
        minValue = minY,
        maxValue = maxY
    )
}

fun List<DomainGraphSeries>.mapToUiChartList(
    canvas: IntSize,
    stepCounterXAxis: Int
): List<Chart> {
    return this.map { it.toUiChart(canvas, stepCounterXAxis) }
}