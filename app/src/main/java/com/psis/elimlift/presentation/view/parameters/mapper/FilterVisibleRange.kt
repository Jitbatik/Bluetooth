package com.psis.elimlift.presentation.view.parameters.mapper

import com.psis.transfer.chart.domain.model.GraphSeries

fun List<GraphSeries>.filterVisibleRange(
    startX: Float,
    count: Int,
): List<GraphSeries> {
    val endX = startX + count

    // Шаг 1: фильтрация по диапазону
    val filtered = map { series ->
        val filteredPoints = series.points
            .asSequence()
            .filter { it.xCoordinate in startX..endX }
            .sortedBy { it.xCoordinate }
            .toMutableList()

        series.copyWithPoints(filteredPoints)
    }

    // Шаг 2: находим минимальное значение x среди всех точек
    val allFilteredPoints = filtered.flatMap { it.points }
    val minX = allFilteredPoints.minByOrNull { it.xCoordinate }?.xCoordinate ?: 0f

    // Шаг 3: нормализуем все x на основе minX
    val normalized = filtered.mapIndexed { _, series ->
        val newPoints = series.points.map { point ->
            point.copy(xCoordinate = point.xCoordinate - minX)
        }.toMutableList()
        series.copyWithPoints(newPoints)
    }

    return normalized
}