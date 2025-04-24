package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.transfer.model.LiftParameters

typealias CanvasPoint = Pair<Int, Int>

@Composable
fun DrawGraph(
    parameters: List<LiftParameters>,
    stepCounterXAxis: Int,
    selectedIndex: Int?,
    onStepSizeXAxisChange: (Float) -> Unit,
    onStepSizeYAxisChange: (Float) -> Unit,
    modifier: Modifier,
    lineColors: List<Color> = listOf(Color.Red, Color.Blue),
    lineWeight: Float = 4f,
) {
    val numberOfGraphs = parameters.firstOrNull()?.data?.size ?: 0
    Column(modifier = modifier) {
        repeat(numberOfGraphs) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
            ) {
                GraphCanvas(
                    dataPoints = parameters.toCanvasPoints(index),
                    stepCounterXAxis = stepCounterXAxis,
                    selectedIndex = selectedIndex,
                    onStepSizeXAxisChange = onStepSizeXAxisChange,
                    onStepSizeYAxisChange = onStepSizeYAxisChange,
                    color = lineColors.getOrNull(index) ?: Color.Gray,
                    lineWeight = lineWeight,
                )
            }
        }
    }
}

fun List<LiftParameters>.calculateBaseTime(): Long {
    return if (isEmpty()) 0L
    else minOf { it.timeStamp * 1000 + it.timeMilliseconds }
}

fun List<LiftParameters>.toCanvasPoints(index: Int): List<CanvasPoint> {
    if (isEmpty()) return emptyList()
    val baseTimeMs = calculateBaseTime()

    return mapNotNull { param ->
        if (index >= param.data.size) null
        else param.toCanvasPoint(baseTimeMs, index)
    }
}

private fun LiftParameters.toCanvasPoint(baseTimeMs: Long, index: Int): CanvasPoint {
    val fullTimeMs = timeStamp * 1000 + timeMilliseconds
    val relativeTimeMs = (fullTimeMs - baseTimeMs).toInt()
    return CanvasPoint(relativeTimeMs, data[index].value)
}

//todo без учета мс толко мало но пока такой вариант
@Composable
private fun GraphCanvas(
    dataPoints: List<CanvasPoint>,
    stepCounterXAxis: Int,
    selectedIndex: Int?,
    onStepSizeXAxisChange: (Float) -> Unit,
    onStepSizeYAxisChange: (Float) -> Unit,
    color: Color,
    lineWeight: Float,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clipToBounds()
        ) {
            if (dataPoints.isEmpty()) return@Canvas

            val yValues = dataPoints.map { it.second }
            val rawMinY = yValues.minOrNull()?.toFloat() ?: 0f
            val rawMaxY = yValues.maxOrNull()?.toFloat() ?: 1f
            val rangeY = (rawMaxY - rawMinY).takeIf { it != 0f } ?: 1f

            val minY = rawMinY - rangeY * 0.1f
            val maxY = rawMaxY + rangeY * 0.1f
            val fullRangeY = maxY - minY

            val stepX = size.width / (stepCounterXAxis - 1).coerceAtLeast(1)
            val stepY = size.height / fullRangeY

            onStepSizeXAxisChange(stepX)
            onStepSizeYAxisChange(stepY)

            fun pointOffset(index: Int, y: Int): Offset {
                val x = index * stepX
                val yMapped = size.height - (y - minY) * stepY
                return Offset(x, yMapped)
            }

            val pointPath = Path().apply {
                dataPoints.forEachIndexed { index, (_, y) ->
                    val point = pointOffset(index, y)
                    if (index == 0) moveTo(point.x, point.y)
                    else lineTo(point.x, point.y)
                }
            }

            val shadowPath = Path().apply {
                var lastX = 0f
                dataPoints.forEachIndexed { index, (_, y) ->
                    val point = pointOffset(index, y)
                    if (index == 0) moveTo(point.x, point.y)
                    else lineTo(point.x, point.y)
                    lastX = point.x
                }
                lineTo(lastX, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = shadowPath,
                brush = Brush.verticalGradient(
                    0f to color.copy(alpha = 0.4f),
                    0.6f to color.copy(alpha = 0.2f),
                    1f to Color.Transparent
                )
            )

            drawPath(
                path = pointPath,
                color = color,
                style = Stroke(width = lineWeight, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            selectedIndex?.takeIf { it in dataPoints.indices }?.let {
                val (x, y) = pointOffset(it, dataPoints[it].second)
                drawCircle(
                    color = color,
                    radius = lineWeight * 2.5f,
                    center = Offset(x, y)
                )
            }
        }

        Text(
            text = dataPoints.lastOrNull()?.second?.toString() ?: "",
            modifier = Modifier
                .width(48.dp)
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterVertically),
            color = Color.White
        )
    }
}



//todo сделать Preview и отрефакторить