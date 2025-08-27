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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.parameters.ui.ChartBuilder

/** Отрисовка графика и выделенной точки*/
@Composable
fun LineChartCanvas(
    points: List<Offset>,
    color: Color,
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
        // draw Line with Shadow
        drawGraphLine(points, color, style)

        // draw Line Shadow
        drawGraphShadow(points, color)

        // draw Point
        selectedIndex?.let { index ->
            points.getOrNull(index)?.let { point ->
                drawSelectedPoint(point, color, style)
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

private fun DrawScope.drawGraphShadow(
    points: List<Offset>,
    color: Color
) {
    if (points.size < 2) return

    val baseline = size.height
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        lineTo(points.last().x, baseline)
        lineTo(points.first().x, baseline)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.4f), // у линии
                color.copy(alpha = 0f)    // вниз в прозрачность
            ),
            startY = points.minOf { it.y },
            endY = baseline
        )
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
    val points = remember(canvasSize) { ChartBuilder(canvasSize).points() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        LineChartCanvas(
            points = points,
            color = Color.Red,
            selectedIndex = 2,
            onCanvasSizeChange = { canvasSize = it },
            modifier = Modifier.fillMaxSize(),
            style = ChartStyle()
        )
    }
}