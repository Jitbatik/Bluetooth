package com.psis.elimlift.presentation.view.parameters.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.psis.elimlift.presentation.view.parameters.model.Chart
import com.psis.elimlift.presentation.view.parameters.util.toUiColor
import com.psis.transfer.chart.domain.model.Line
import com.psis.transfer.chart.domain.model.SignalUserSettings
import com.psis.transfer.chart.domain.model.timeMs
import kotlin.math.max

/**
 * Мапер данных из домейн слоя в UI
 *
 * @param canvas - размер элемента Canvas из UI, нужен для рассчета шага
 * @param stepCounterXAxis - количество шагов на графике в секундах
 * @param signalUserSettings - пользовательские настройки отображения графиков
 *
 * @return List<Chart> - список графиков с нормализацией под UI
 */
fun List<Line>.mapListLineToUiChartList(
    canvas: IntSize,
    stepCounterXAxis: Int,
    signalUserSettings: List<SignalUserSettings>,
): List<Chart> {
    if (isEmpty()) return emptyList()

    val signalSettingsMap = signalUserSettings.associateBy { it.name }

    val visibleLines = this.filter { line ->
        val settings = signalSettingsMap[line.name]
        settings?.isVisible ?: true
    }

    if (visibleLines.isEmpty()) return emptyList()

    val allPoints = visibleLines.flatMap { it.points }
    val startTimeMs = allPoints.minOfOrNull { it.timeMs() } ?: return emptyList()
    val stepX = canvas.width / stepCounterXAxis.toFloat()

    return visibleLines.mapNotNull { line ->
        if (line.points.isEmpty()) return@mapNotNull null

        val settings = signalSettingsMap[line.name]
        val lineColor = settings?.color?.toUiColor() ?: Color(0, 0, 0)

        val sortedPoints = line.points.sortedBy { it.timeMs() }

        val values = sortedPoints.map { it.value.toFloat() }
        val minY = 0f
        val maxY = max(1f, values.maxOrNull() ?: 1f)
        val yRange = max(1f, maxY - minY)
        val stepY = canvas.height / yRange

        val points = sortedPoints.map { point ->
            val x = (point.timeMs() - startTimeMs) / 1000f * stepX
            val y = canvas.height - (point.value - minY) * stepY
            Offset(x, y)
        }

        Chart(
            name = line.name,
            points = points,
            color = lineColor,
            minValue = minY,
            maxValue = maxY,
        )
    }
}