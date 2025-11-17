package com.psis.elimlift.presentation.view.parameters.mapper

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.psis.elimlift.presentation.view.parameters.model.Chart
import com.psis.elimlift.presentation.view.parameters.util.toUiColor
import com.psis.transfer.chart.domain.usecase.ChartFrame
import kotlin.math.max
import com.psis.transfer.chart.domain.model.GraphSeries as DomainGraphSeries

fun List<ChartFrame>.mapToUiChartList22(
    canvas: IntSize,
    stepCounterXAxis: Int
): List<Chart> {

    if (isEmpty()) return emptyList()

    fun ChartFrame.timeMs(): Long = timestampSeconds * 1000L + millis

    val minTime = minByOrNull { it.timeMs() } ?: return emptyList()


    val stepX = canvas.width / stepCounterXAxis.toFloat()
    // группируем сигналы по имени
    val signalsByName = flatMap { frame ->
        val time = frame.timeMs()
        frame.signals.map { signal -> signal.name to (time to signal) }
    }.groupBy({ it.first }, { it.second })

    val result =signalsByName.map { (name, timeSignalPairs) ->
        // сортируем по времени
        val sorted = timeSignalPairs.sortedBy { it.first }
        val values = sorted.map { it.second.value.toFloat() }

        val minY = 0f
        val maxY = max(1f, values.maxOrNull() ?: 1f)
        val yRange = max(1f, maxY - minY)
        val stepY = canvas.height / yRange

        // нормализуем точки под Canvas (Offset)
        val points = sorted.mapIndexed { index, (time, signal) ->
            val x = (time - minTime.timeMs()) / 1000f * stepX
            val y = canvas.height - (signal.value - minY) * stepY
            val offset = Offset(x, y)

            offset
        }


        val color = sorted.firstNotNullOfOrNull { it.second.color?.toUiColor() } ?: Color.Gray

        Chart(
            name = name,
            points = points,
            color = color,
            minValue = minY,
            maxValue = maxY
        )
    }
    return result
}


fun DomainGraphSeries.toUiChart(
    canvas: IntSize,
    stepCounterXAxis: Int
): Chart {
    val points = this.points
    val minY = 0f
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