package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.transfer.model.LiftParameterType
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParameterData
import com.example.transfer.model.ParameterType
import kotlin.math.cos
import kotlin.math.sin

typealias ParameterPaths = Pair<Path, Path>

/**
 * Стиль отрисовки графика
 */
data class GraphStyle(
    val lineStroke: Stroke = Stroke(
        width = 4f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    ),
    val verticalOffset: Float = 10f,
    val shadowAlpha: Float = 0.3f,
    val pointRadiusFactor: Float = 10f,
    val valueFormatter: (Float) -> String = { "%.1f".format(it) }
)

/**
 * Метрики графика
 */
private data class GraphMetrics(
    val baseTimeMs: Long,
    val minYValues: Map<ParameterType, Float>,
    val maxYValues: Map<ParameterType, Float>,
    val yRanges: Map<ParameterType, Float>
)

/**
 * Конвертер координат для графика
 */
private class GraphCoordinateConverter(
    private val baseTimeMs: Long,
    private val stepX: Float,
    private val graphHeight: Float,
    private val yRanges: Map<ParameterType, Float>,
    private val minYValues: Map<ParameterType, Float>,
    private val parameterIndex: Map<ParameterType, Int>
) {
    fun convert(param: LiftParameters, value: Float, paramType: ParameterType): Offset {
        val timeMs = param.timestamp * 1000 + param.timeMilliseconds
        val x = ((timeMs - baseTimeMs) / 1000f) * stepX
        
        val yRange = yRanges[paramType]?.coerceAtLeast(1f) ?: 1f
        val minY = minYValues[paramType] ?: 0f
        val stepY = graphHeight / yRange
        
        val baseY = if (yRange <= 1f) {
            // Для сигналов с постоянным значением добавляем смещение на основе индекса
            val index = parameterIndex[paramType] ?: 0
            val offset = (graphHeight * 0.1f) // 10% от высоты графика
            val spacing = offset / (parameterIndex.size + 1) // Равномерное распределение
            graphHeight / 2 + (index - parameterIndex.size / 2f) * spacing
        } else {
            // Обычное вычисление для изменяющихся сигналов
            graphHeight - (value - minY) * stepY
        }
        return Offset(x, baseY)
    }
}

/**
 * Компонент для отрисовки линейного графика.
 *
 * @param parameters Список параметров для отображения
 * @param stepCountXAxis Количество делений по оси X
 * @param selectedIndex Индекс выбранной точки для подсветки
 * @param onStepSizeXAxisChange Callback изменения размера шага по оси X
 * @param onStepSizeYAxisChange Callback изменения размера шага по оси Y
 * @param modifier Модификатор для настройки внешнего вида
 * @param lineColors Карта цветов для разных типов параметров
 * @param style Стиль отрисовки графика
 */
@Composable
fun DrawGraph(
    parameters: List<LiftParameters>,
    stepCountXAxis: Int,
    selectedIndex: Int?,
    onStepSizeXAxisChange: (Float) -> Unit,
    onStepSizeYAxisChange: (Float) -> Unit,
    modifier: Modifier,
    lineColors: Map<ParameterType, Color>,
    style: GraphStyle = GraphStyle()
) {
    if (parameters.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val metrics = rememberGraphMetrics(parameters)

    val groupedData by remember(parameters) {
        derivedStateOf { parameters.groupedByParameterLabel() }
    }

    // Создаем индекс для каждого типа параметра
    val parameterIndices = remember(groupedData) {
        groupedData.keys.mapIndexed { index, type -> type to index }.toMap()
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

            onStepSizeXAxisChange(stepX)

            val converter = GraphCoordinateConverter(
                baseTimeMs = metrics.baseTimeMs,
                stepX = stepX,
                graphHeight = graphHeight,
                yRanges = metrics.yRanges,
                minYValues = metrics.minYValues,
                parameterIndex = parameterIndices
            )

            groupedData.entries.forEachIndexed { index, (label, values) ->
                val color = lineColors[label] ?: Color.Gray
                val stepY = graphHeight / (metrics.yRanges[label] ?: 1f)
                onStepSizeYAxisChange(stepY)

                drawParameterLine(
                    label = label,
                    values = values,
                    converter = converter,
                    index = index,
                    color = color,
                    style = style
                )
            }

            selectedIndex?.let { idx ->
                drawSelectedPoint(
                    parameters = parameters,
                    index = idx,
                    lineColors = lineColors,
                    converter = converter,
                    pointRadiusFactor = style.pointRadiusFactor
                )
            }
        }

        if (parameters.isNotEmpty()) {
            ValuesPanel(
                parameter = parameters.last(),
                lineColors = lineColors,
                minValues = metrics.minYValues,
                maxValues = metrics.maxYValues,
                valueFormatter = style.valueFormatter,
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun rememberGraphMetrics(parameters: List<LiftParameters>): GraphMetrics {
    return remember(parameters) {
        val base = parameters.minOfOrNull { it.combinedTimeMs } ?: 0L
        val groupedData = parameters.groupedByParameterLabel()

        val minValues = groupedData.mapValues { (_, values) ->
            values.minOfOrNull { it.second } ?: 0f
        }

        val maxValues = groupedData.mapValues { (_, values) ->
            values.maxOfOrNull { it.second } ?: 0f
        }

        val ranges = maxValues.mapValues { (paramType, maxY) ->
            val minY = minValues[paramType] ?: 0f
            (maxY - minY).coerceAtLeast(1f)
        }

        GraphMetrics(base, minValues, maxValues, ranges)
    }
}

private val LiftParameters.combinedTimeMs: Long
    get() = timestamp * 1000L + timeMilliseconds

private fun List<LiftParameters>.groupedByParameterLabel(): Map<ParameterType, List<Pair<LiftParameters, Float>>> {
    return flatMap { param ->
        param.parameters.map { data -> data.label to (param to data.value.toFloat()) }
    }.groupBy({ it.first }, { it.second })
}

private fun DrawScope.drawParameterLine(
    label: ParameterType,
    values: List<Pair<LiftParameters, Float>>,
    converter: GraphCoordinateConverter,
    index: Int,
    color: Color,
    style: GraphStyle
) {
    val paths = calculatePaths(
        values = values,
        converter = { param, value -> converter.convert(param, value, label) },
        index = index,
        verticalOffset = style.verticalOffset,
        graphHeight = size.height
    )

    drawPath(
        path = paths.first,
        color = color,
        style = style.lineStroke
    )
    drawPath(
        path = paths.second,
        brush = Brush.verticalGradient(
            listOf(color.copy(alpha = style.shadowAlpha), Color.Transparent)
        )
    )
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
    lineColors: Map<ParameterType, Color>,
    converter: GraphCoordinateConverter,
    pointRadiusFactor: Float
) {
    if (index < 0) return
    
    parameters.getOrNull(index)?.let { selectedParam ->
        selectedParam.parameters.forEach { data ->
            val color = lineColors[data.label] ?: Color.Gray
            val point = converter.convert(selectedParam, data.value.toFloat(), data.label)
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
    lineColors: Map<ParameterType, Color>,
    minValues: Map<ParameterType, Float>,
    maxValues: Map<ParameterType, Float>,
    valueFormatter: (Float) -> String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        parameter.parameters.forEach { parameterData ->
            val color = lineColors[parameterData.label] ?: Color.Gray
            val minValue = minValues[parameterData.label]
            val maxValue = maxValues[parameterData.label]
            
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = valueFormatter(parameterData.value.toFloat()),
                    color = color,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (minValue != null && maxValue != null) {
                    Text(
                        text = "[${valueFormatter(minValue)}-${valueFormatter(maxValue)}]",
                        color = color.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DrawGraphPreview() {
    val sampleData = List(100) { index ->
        LiftParameters(
            timestamp = index.toLong(),
            timeMilliseconds = 0,
            parameters = listOf(
                ParameterData(
                    LiftParameterType.ENCODER_FREQUENCY,
                    (sin(index * 0.1) * 50 + 50).toInt()
                ),
                ParameterData(
                    LiftParameterType.ELEVATOR_SPEED,
                    (cos(index * 0.1) * 30 + 30).toInt()
                )
            ),
            frameId = 0
        )
    }

    val colors: Map<ParameterType, Color> = mapOf(
        LiftParameterType.ENCODER_FREQUENCY to Color.Blue,
        LiftParameterType.ELEVATOR_SPEED to Color.Red
    )

    DrawGraph(
        parameters = sampleData,
        stepCountXAxis = 10,
        selectedIndex = 5,
        onStepSizeXAxisChange = {},
        onStepSizeYAxisChange = {},
        modifier = Modifier.fillMaxSize(),
        lineColors = colors
    )
}