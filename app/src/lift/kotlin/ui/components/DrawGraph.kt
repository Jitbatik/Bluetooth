package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.transfer.model.LiftParameters

typealias CanvasPoint = Pair<Int, Int>

@Composable
fun DrawGraph(
    parameters: List<LiftParameters>,
    stepCounterXAxis: Int,
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
                    onStepSizeXAxisChange = onStepSizeXAxisChange,
                    onStepSizeYAxisChange = onStepSizeYAxisChange,
                    color = lineColors.getOrNull(index) ?: Color.Gray,
                    lineWeight = lineWeight,
                )
            }
        }
    }
}

fun List<LiftParameters>.toCanvasPoints(index: Int): List<CanvasPoint> {
    if (this.isEmpty()) return emptyList()
    val baseTimeMs = this.minOf { it.timeStamp * 1000 + it.timeMilliseconds }

    return this.mapNotNull { param ->
        if (index >= param.data.size) return@mapNotNull null

        val fullTimeMs = param.timeStamp * 1000 + param.timeMilliseconds
        val relativeTimeMs = (fullTimeMs - baseTimeMs).toInt()

        CanvasPoint(relativeTimeMs, param.data[index])
    }
}

@Composable
private fun GraphCanvas(
    dataPoints: List<Pair<Int, Int>>,
    stepCounterXAxis: Int,
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

            val dataValues = dataPoints.map { it.second }

            val minYf = dataValues.minOrNull()?.toFloat() ?: 0f
            val maxYf = dataValues.maxOrNull()?.toFloat() ?: 1f
            val rangeY = (maxYf - minYf).takeIf { it != 0f } ?: 1f

            val stepY = size.height / rangeY
            val segments = (stepCounterXAxis - 1).coerceAtLeast(1)
            val stepX = size.width / segments

            onStepSizeXAxisChange(stepX)
            onStepSizeYAxisChange(stepY)

            val path = Path().apply {
                dataPoints.forEachIndexed { index, (_, y) ->
                    val xPos = index * stepX
                    val yPos = size.height - (y - minYf) * stepY

                    if (index == 0) moveTo(xPos, yPos)
                    else lineTo(xPos, yPos)
                }
            }

            val shadowPath = Path().apply {
                var lastX = 0f
                dataPoints.forEachIndexed { index, (_, y) ->
                    val xPos = index * stepX
                    val yPos = size.height - (y - minYf) * stepY
                    if (index == 0) {
                        moveTo(xPos, yPos)
                    } else {
                        lineTo(xPos, yPos)
                    }
                    lastX = xPos
                }
                lineTo(lastX, size.height)
                lineTo(0f, size.height)
                close()
            }


            val gradient = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to color.copy(alpha = 0.4f),
                    0.6f to color.copy(alpha = 0.2f),
                    1.0f to Color.Transparent
                ),
                startY = maxYf,
                endY = size.height
            )

            drawPath(path = shadowPath, brush = gradient, style = Fill)
            drawPath(
                path = path,
                color = color,
                style = Stroke(lineWeight, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )


        }
        Text(
            text = dataPoints.lastOrNull()?.second?.toString() ?: "",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterVertically),
            color = Color.White
        )
    }
}


//todo сделать Preview и отрефакторить