package com.example.bluetooth.presentation.view.parameters.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.bluetooth.presentation.view.parameters.model.DataPoint
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.ui.GraphTransformer
import com.example.bluetooth.presentation.view.parameters.util.computePerSeriesLayout

@Composable
fun LineChartCanvas(
    chartData: List<GraphSeries>,
    stepCountXAxis: Int,
    selectedIndex: Int?,
    onStepSizeXAxisChange: (Float) -> Unit,
    style: ChartStyle,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.clipToBounds()) {
        val topBottomPadding = size.height * 0.05f
        val sidePadding = size.width * 0.01f

        val drawingWidth = size.width - sidePadding * 2
        val drawingHeight = size.height - topBottomPadding * 2

        val layout = computePerSeriesLayout(
            chartData = chartData,
            width = drawingWidth,
            height = drawingHeight,
            stepCountXAxis = stepCountXAxis
        )

        onStepSizeXAxisChange(layout.stepX)

        withTransform({
            translate(left = sidePadding, top = topBottomPadding)
        }) {
            chartData.forEach { graphLine ->
                val transformer = layout.transformers[graphLine.name] ?: return@forEach

                drawGraphLine(this, graphLine, transformer, style)

                selectedIndex?.let { selected ->
                    graphLine.points.getOrNull(selected)?.let { point ->
                        drawSelectedPoint(this, point, transformer, graphLine.color, style)
                    }
                }
            }
        }
    }
}

private fun drawSelectedPoint(
    drawScope: DrawScope,
    point: DataPoint,
    transformer: GraphTransformer,
    color: Color,
    style: ChartStyle
) = with(drawScope) {
    val x = transformer.toCanvasX(point.xCoordinate)
    val y = transformer.toCanvasY(point.yCoordinate)
    val radius = style.pointRadiusFactor

    drawCircle(
        color = color,
        radius = radius * 0.8f,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color.White,
        radius = radius,
        center = Offset(x, y),
        style = style.lineStroke
    )
}

private fun drawGraphLine(
    drawScope: DrawScope,
    line: GraphSeries,
    transformer: GraphTransformer,
    style: ChartStyle
) = with(drawScope) {
    val path = Path()
    var firstPoint = true
    val xPoints = mutableListOf<Float>()

    line.points.forEach { point ->
        val x = transformer.toCanvasX(point.xCoordinate)
        val y = transformer.toCanvasY(point.yCoordinate)
        xPoints.add(x)
        if (firstPoint) {
            path.moveTo(x, y)
            firstPoint = false
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(path = path, color = line.color, style = style.lineStroke)

    val minX = xPoints.minOrNull() ?: 0f
    val maxX = xPoints.maxOrNull() ?: 0f

    val shadowPath = Path().apply {
        addPath(path)
        lineTo(maxX, drawScope.size.height)
        lineTo(minX, drawScope.size.height)
        close()
    }

    drawScope.drawPath(
        path = shadowPath,
        brush = Brush.verticalGradient(
            listOf(line.color.copy(alpha = style.shadowAlpha), Color.Transparent)
        )
    )
}