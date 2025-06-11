package com.example.bluetooth.presentation.view.parameters.util

import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.ui.GraphTransformer

data class PerSeriesGraphLayout(
    val stepX: Float,
    val transformers: Map<String, GraphTransformer>
)

fun computePerSeriesLayout(
    chartData: List<GraphSeries>,
    width: Float,
    height: Float,
    stepCountXAxis: Int,
): PerSeriesGraphLayout {
    val stepX = width / stepCountXAxis.coerceAtLeast(1)
    val allPoints = chartData.flatMap { it.points }
    val minX = allPoints.minOfOrNull { it.xCoordinate } ?: 0f

    val transformers = chartData.mapIndexed { index, series ->
        val minY = series.points.minOfOrNull { it.yCoordinate } ?: 0f
        val maxY = series.points.maxOfOrNull { it.yCoordinate } ?: 1f
        val isFlat = minY == maxY

        val offset = if (isFlat) (index - chartData.size / 2f) * 0.2f else 0f
        val adjustedMinY = if (isFlat) minY + offset - 0.5f else minY
        val adjustedMaxY = if (isFlat) maxY + offset + 0.5f else maxY

        val safeRange = (adjustedMaxY - adjustedMinY).coerceAtLeast(1f)
        val stepY = height / safeRange

        val transformer = GraphTransformer(
            minX = minX,
            minY = adjustedMinY,
            stepX = stepX,
            stepY = stepY,
            height = height
        )

        series.name to transformer
    }.toMap()

    return PerSeriesGraphLayout(stepX, transformers)
}

