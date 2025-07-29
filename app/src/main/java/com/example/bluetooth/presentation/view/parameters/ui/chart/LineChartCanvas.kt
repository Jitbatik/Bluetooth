package com.example.bluetooth.presentation.view.parameters.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries

/***
 * Отрисовка графика и выделенной точки
 * ***/
@Composable
fun LineChartCanvas(
    chartData: List<GraphSeries>,
    selectedIndex: Int?,
    onCanvasSizeChange: (IntSize) -> Unit,
    style: ChartStyle,
    modifier: Modifier = Modifier,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val currentOnCanvasSizeChange by rememberUpdatedState(onCanvasSizeChange)

    LaunchedEffect(canvasSize) {
        currentOnCanvasSizeChange(canvasSize)
    }

    Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
        // draw Line
        chartData.forEach { series ->
            drawGraphLine(series.points, series.color, style)
        }

        // draw Point
        selectedIndex?.let { index ->
            chartData.forEach { series ->
                series.points.getOrNull(index)?.let { point ->
                    drawSelectedPoint(point, series.color, style)
                }
            }
        }
    }
}


private fun DrawScope.drawSelectedPoint(
    point: Offset,
    color: Color,
    style: ChartStyle
) {
    drawCircle(
        color = color,
        radius = style.pointRadiusFactor * 0.8f,
        center = point
    )
    drawCircle(
        color = Color.White,
        radius = style.pointRadiusFactor,
        center = point,
        style = style.lineStroke
    )
}

private fun DrawScope.drawGraphLine(
    points: List<Offset>,
    color: Color,
    style: ChartStyle
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }

    drawPath(
        path = path,
        color = color,
        style = style.lineStroke
    )
}


@Preview(showBackground = true)
@Composable
private fun LineChartContentPreview() {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val chartData = remember(canvasSize) {
        generateFakeChartData(canvasSize = canvasSize)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        LineChartCanvas(
            chartData = chartData,
            selectedIndex = 2,
            onCanvasSizeChange = { canvasSize = it },
            modifier = Modifier
                .fillMaxSize(),
            style = ChartStyle()
        )
    }
}

fun generateFakeChartData(
    canvasSize: IntSize,
    stepCountXAxis: Int = 20,
    stepCountYAxis: Int = 20,
    seriesCount: Int = 7,
    minXStep: Int = 0,
    maxXStep: Int = 100,
    minY: Int = 1,
    maxY: Int = 18,
    totalX: Float = 19f
): List<GraphSeries> {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) return emptyList()

    val stepX = canvasSize.width.toFloat() / stepCountXAxis.coerceAtLeast(1)
    val stepY = canvasSize.height.toFloat() / stepCountYAxis.coerceAtLeast(1)
    val height = canvasSize.height.toFloat()

    val xCoordinates = buildList {
        var currentX = 0f
        while (currentX < totalX) {
            add(currentX)
            currentX += (minXStep..maxXStep).random() / 100f
        }
    }

    return List(seriesCount) { index ->
        val points = xCoordinates.map { x ->
            val y = (minY..maxY).random().toFloat()
            Offset(x * stepX, height - y * stepY)
        }

        GraphSeries(
            name = "Series $index",
            points = points.sortedBy { it.x },
            color = Color(
                red = (0..255).random(),
                green = (0..255).random(),
                blue = (0..255).random()
            )
        )
    }
}
