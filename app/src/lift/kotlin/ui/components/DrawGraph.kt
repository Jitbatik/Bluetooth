package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParameterLabel

typealias ParameterPaths = Pair<Path, Path>


@Composable
fun DrawGraph(
    parameters: List<LiftParameters>,
    stepCountXAxis: Int,
    selectedIndex: Int?,
    onStepSizeXAxisChange: (Float) -> Unit,
    onStepSizeYAxisChange: (Float) -> Unit,
    modifier: Modifier,
    lineColors: Map<ParameterLabel, Color>,
    verticalOffset: Float = 10f,
    shadowAlpha: Float = 0.3f,
    pointRadiusFactor: Float = 10f,
    lineStroke: Stroke = Stroke(
        width = 4f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
) {
    val (baseTimeMs, minY, maxY) = rememberCalculatedMetrics(parameters)
    val yRange = remember(maxY, minY) { (maxY - minY).coerceAtLeast(1f) }

    val groupedData by remember(parameters) {
        derivedStateOf { parameters.groupedByParameterLabel() }
    }

    Row(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clipToBounds()
        ) {
            val graphHeight = size.height

            val stepX = size.width / stepCountXAxis.coerceAtLeast(1)
            val stepY = graphHeight / yRange

            onStepSizeXAxisChange(stepX)
            onStepSizeYAxisChange(stepY)

            val convertToPoint: (LiftParameters, Float) -> Offset = { param, value ->
                val timeMs = param.timeStamp * 1000 + param.timeMilliseconds
                val x = ((timeMs - baseTimeMs) / 1000f) * stepX
                val baseY = graphHeight - (value - minY) * stepY
                Offset(x, baseY)
            }

            groupedData.entries.forEachIndexed { index, (label, values) ->
                val color = lineColors[label] ?: Color.Gray

                val paths = calculatePaths(
                    values = values,
                    converter = convertToPoint,
                    index = index,
                    verticalOffset = verticalOffset,
                    graphHeight = graphHeight
                )

                drawPath(
                    path = paths.first,
                    color = color,
                    style = lineStroke
                )
                drawPath(
                    path = paths.second,
                    brush = Brush.verticalGradient(
                        listOf(color.copy(alpha = shadowAlpha), Color.Transparent)
                    )
                )

                selectedIndex?.let { idx ->
                    drawSelectedPoint(
                        parameters = parameters,
                        index = idx,
                        lineColors = lineColors,
                        converter = convertToPoint,
                        pointRadiusFactor = pointRadiusFactor
                    )
                }
            }
        }

        if (parameters.isNotEmpty()) {
            ValuesPanel(
                parameter = parameters.last(),
                lineColors = lineColors,
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun rememberCalculatedMetrics(parameters: List<LiftParameters>): Triple<Long, Float, Float> {
    return remember(parameters) {
        val base = parameters.minOfOrNull { it.combinedTimeMs } ?: 0L
        val allValues = parameters.flatMap { it.data.map { data -> data.value } }
        val minVal = allValues.minOrNull()?.toFloat() ?: 0f
        val maxVal = allValues.maxOrNull()?.toFloat() ?: 0f
        Triple(base, minVal, maxVal)
    }
}

private val LiftParameters.combinedTimeMs: Long
    get() = timeStamp * 1000L + timeMilliseconds

private fun List<LiftParameters>.groupedByParameterLabel(): Map<ParameterLabel, List<Pair<LiftParameters, Float>>> {
    return flatMap { param ->
        param.data.map { data -> data.label to (param to data.value.toFloat()) }
    }.groupBy({ it.first }, { it.second })
}

private fun calculatePaths(
    values: List<Pair<LiftParameters, Float>>,
    converter: (LiftParameters, Float) -> Offset,
    index: Int,
    verticalOffset: Float,
    graphHeight: Float
): ParameterPaths {
    val linePath = Path()
    val shadowBasePath = Path()
    var firstPoint = true
    var lastX = 0f

    values.forEach { (param, value) ->
        val basePoint = converter(param, value)
        val offsetPoint = basePoint.copy(y = basePoint.y + (index % 3 - 1) * verticalOffset)

        if (firstPoint) {
            linePath.moveTo(offsetPoint.x, offsetPoint.y)
            shadowBasePath.moveTo(basePoint.x, basePoint.y)
            firstPoint = false
        } else {
            linePath.lineTo(offsetPoint.x, offsetPoint.y)
            shadowBasePath.lineTo(basePoint.x, basePoint.y)
        }
        lastX = basePoint.x
    }

    val shadowPath = Path().apply {
        addPath(shadowBasePath)
        if (values.isNotEmpty()) {
            lineTo(lastX, graphHeight)
            lineTo(
                x = values.first().let { converter(it.first, it.second).x },
                y = graphHeight
            )
            close()
        }
    }

    return ParameterPaths(linePath, shadowPath)
}


private fun DrawScope.drawSelectedPoint(
    parameters: List<LiftParameters>,
    index: Int,
    lineColors: Map<ParameterLabel, Color>,
    converter: (LiftParameters, Float) -> Offset,
    pointRadiusFactor: Float
) {
    parameters.getOrNull(index)?.let { selectedParam ->
        selectedParam.data.forEach { data ->
            val color = lineColors[data.label] ?: Color.Gray
            val point = converter(selectedParam, data.value.toFloat())
            drawCircle(
                color = color,
                radius = pointRadiusFactor,
                center = point
            )
        }
    }
}

@Composable
private fun ValuesPanel(
    parameter: LiftParameters,
    lineColors: Map<ParameterLabel, Color>,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        parameter.data.forEach { test ->
            Text(
                text = "${test.value}",
                color = lineColors[test.label] ?: Color.Gray,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}


//todo сделать Preview и отрефакторить