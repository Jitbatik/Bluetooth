package com.example.bluetooth.presentation.view.parameters.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.util.toUiColor
import javax.inject.Inject
import com.example.transfer.chart.domain.model.GraphSeries as DomainGraphSeries

fun DomainGraphSeries.toUiSeries(
    stepX: Float,
    stepY: Float,
    height: Float
) = GraphSeries(
    name = this.name,
    points = this.points.map { (x, y) ->
        Offset(
            x = x * stepX,
            y = height - y * stepY // переворот оси Y
        )
    },
    color = this.color?.toUiColor() ?: Color.Black
)

fun List<DomainGraphSeries>.mapToUiSeriesList(
    canvas: IntSize,
    stepCounterXAxis: Int,
    stepCountYAxis: Int
): List<GraphSeries> {
    val stepX = canvas.width / stepCounterXAxis.toFloat()
    val stepY = canvas.height / stepCountYAxis.toFloat()

    return this
        .map { it.toUiSeries(stepX, stepY, canvas.height.toFloat()) }
}

fun List<DomainGraphSeries>.maxY(): Float {
    return this.flatMap { it.points.map { point -> point.yCoordinate } }
        .maxOrNull() ?: 0f
}

fun List<DomainGraphSeries>.minY(): Float {
    return this.flatMap { it.points.map { point -> point.yCoordinate } }
        .minOrNull() ?: 0f
}

fun List<GraphSeries>.applyJitter(
    offsetPx: Float,
    canvasHeight: Float
): List<GraphSeries> {
    if (isEmpty()) return this
    val midIndex = (size - 1) / 2f
    return mapIndexed { index, series ->
        val relative = (index - midIndex) * offsetPx
        series.copy(
            points = series.points.map { point ->
                // смещаем и ограничиваем в пределах [0, canvasHeight]
                val y = (point.y + relative).coerceIn(0f, canvasHeight)
                Offset(point.x, y)
            }
        )
    }
}
