package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.ParametersIntent
import com.example.transfer.model.AxisOrientation
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.Parameter
import com.example.transfer.model.ParameterPoint
import com.example.transfer.model.ParametersLabel
import com.example.transfer.model.TickLabel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max


@Composable
fun LineChart(
    parameter: Parameter,
    chartParameters: ChartParameters,
    onEvents: (Int, ParametersIntent) -> Unit,
) {
    val points = parameter.points.safeSubList(chartParameters.offset.toInt(), parameter.stepCount)
    if (points.isEmpty()) return

    val minXAxis = points.minOf { it.timeStamp }
    val maxXAxis = points.maxOf { it.timeStamp }

    val minYAxis = points.minOf { it.value }
    val maxYAxis = points.maxOf { it.value }

    var stepSizeX by remember { mutableFloatStateOf(0f) }
    var stepSizeY by remember { mutableFloatStateOf(0f) }

    val currentScale by rememberUpdatedState(chartParameters.scale)
    val currentOffset by rememberUpdatedState(chartParameters.offset)
    val currentMinOffsetX by rememberUpdatedState(chartParameters.minOffsetX)
    val currentMaxOffsetX by rememberUpdatedState(chartParameters.maxOffsetX)

    val handleTransformGesture: (Offset, Float) -> Unit = { pan, zoom ->
        val newScale = (currentScale * zoom)
            .coerceIn(chartParameters.minScale, chartParameters.maxScale)
        val newOffsetX = (currentOffset - pan.x / 10)
            .coerceIn(currentMinOffsetX, currentMaxOffsetX)

        onEvents(parameter.id, ParametersIntent.ChangeOffset(newOffsetX))
        onEvents(parameter.id, ParametersIntent.ChangeScale(newScale))
    }

    val tickProviderXAxis: () -> List<TickLabel> = {
        generateTickAndLabels(
            startValue = minXAxis,
            maxValue = maxXAxis,
            tickCount = 10,
            stepSize = stepSizeX,
            offset = chartParameters.offset,
            scale = chartParameters.scale,
            stepCount = parameter.stepCount,
        )
    }
    val tickProviderYAxis: () -> List<TickLabel> = {
        // todo нужно закончить тики тут
        //  и исправить отображение для удобства убрав промежуток между графиком
        //  перетащить текст в лево от линии
        generateTickAndLabels(
            startValue = minYAxis,
            maxValue = maxYAxis,
            tickCount = 10,
            stepSize = stepSizeY,

            offset = chartParameters.offset,
            scale = chartParameters.scale,
            stepCount = parameter.stepCount,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .height(400.dp),
    ) {
        Text(
            text = "Масштаб: ${chartParameters.scale} | Смещение: ${chartParameters.offset}",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(8.dp)
        )
        Button(
            onClick = {
                onEvents(parameter.id, ParametersIntent.ChangeScale(1f))
                onEvents(parameter.id, ParametersIntent.ChangeOffset(chartParameters.maxOffsetX))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сбросить масштаб и скролл")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            DrawAxis(
                tickProvider = tickProviderYAxis,
                modifier = Modifier.width(10.dp),
                orientation = AxisOrientation.VERTICAL
            )
            DrawGraph(
                parameters = points,
                minXAxis = minXAxis,
                stepSizeYAxis = stepSizeY,
                stepSizeXAxis = stepSizeX,
                stepCounterXAxis = parameter.stepCount,
                onStepSizeYAxisChange = { newParamStepSizeY -> stepSizeY = newParamStepSizeY },
                onStepSizeXAxisChange = { newParamStepSizeX -> stepSizeX = newParamStepSizeX },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            handleTransformGesture(
                                pan,
                                zoom
                            )
                        }
                    }
            )
        }
        DrawAxis(
            modifier = Modifier.height(10.dp),
            tickProvider = tickProviderXAxis
        )
    }
}

fun <T> List<T>.safeSubList(start: Int, count: Int): List<T> {
    val safeStart = start.coerceAtLeast(0).coerceAtMost(size)
    val safeEnd = (start + count).coerceAtMost(size)
    return if (safeStart >= safeEnd) emptyList() else subList(safeStart, safeEnd)
}

@Suppress("SameParameterValue")
private fun generateTickAndLabels(
    startValue: Int,
    maxValue: Int,
    tickCount: Int,
    stepSize: Float,
    offset: Float,
    scale: Float,
    stepCount: Int
): List<TickLabel> {
    val start = (startValue + offset / stepSize).toInt()
    val valueRange = maxValue - startValue
    val tickLabels = mutableListOf<TickLabel>()

    fun addTicks(stepFraction: Double) {
        val step = (tickCount * stepFraction).toInt().coerceAtLeast(2)
        repeat(step) { i ->
            val fraction = i.toDouble() / (step - 1)
            tickLabels.add(
                TickLabel(
                    position = (fraction * valueRange * stepSize).toFloat(),
                    label = formatTimestamp(
                        (start + fraction * valueRange).toInt(),
                        scale
                    )
                )
            )
        }
    }

    fun addBoundaryTicks() {
        tickLabels.apply {
            add(TickLabel(position = 0f, label = formatTimestamp(start, scale)))
            add(
                TickLabel(
                    position = valueRange * stepSize,
                    label = formatTimestamp(minOf(start + stepCount, maxValue), scale)
                )
            )
        }
    }

    val stepFraction = getStepFraction(valueRange.toDouble() / stepCount)
    if (stepFraction == 0.0) addBoundaryTicks() else addTicks(stepFraction)

    return tickLabels
}

private fun formatTimestamp(timestamp: Int, currentScale: Float) =
    DateTimeFormatter.ofPattern(getPatternForScale(currentScale))
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochSecond(timestamp.toLong()))

private fun getPatternForScale(currentScale: Float) = when (currentScale) {
    in 1f..2f -> "mm:ss"
    in 0.25f..1f -> "HH:mm"
    else -> "dd/HH:mm:ss"
}


private fun getStepFraction(relativeRange: Double) = when {
    relativeRange < 0.25 -> 0.0
    relativeRange < 0.5 -> 0.5
    relativeRange < 0.75 -> 0.75
    else -> 1.0
}

@Preview
@Composable
fun LineChartPreview() {
    val parameterLists = remember {
        mutableStateListOf(
            ParameterPoint(77, 10), ParameterPoint(78, 10), ParameterPoint(79, 12),
            ParameterPoint(80, 10), ParameterPoint(81, 10), ParameterPoint(82, 11),
            ParameterPoint(83, 10), ParameterPoint(84, 10), ParameterPoint(85, 12),
            ParameterPoint(86, 3), ParameterPoint(87, 7)
        )
    }

    val minScalePoint = 300
    val maxScalePoint = 10
    val id = 1

    fun interpolateSteps(minScale: Float, maxScale: Float, currentScale: Float): Int =
        (minScalePoint + ((currentScale - minScale) * (maxScalePoint - minScalePoint) / (maxScale - minScale))).toInt()

    fun generateNewPoint(lastPoint: ParameterPoint?): ParameterPoint? {
        return lastPoint?.let {
            val newValue = (it.value + (-5..5).random()).coerceIn(0, 15)
            ParameterPoint(it.timeStamp + 1, newValue)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            generateNewPoint(parameterLists.lastOrNull())?.let { parameterLists.add(it) }
        }
    }

    val chartParameters = remember {
        mutableStateMapOf(
            id to ChartParameters(
                scale = 1f,
                minScale = 1f,
                maxScale = 2f,
                offset = 0f,
                minOffsetX = 0f,
                maxOffsetX = 0f
            )
        )
    }
    var autoScrollEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(parameterLists.size) {
        val params = chartParameters.getOrDefault(id, ChartParameters())
        val stepCount = interpolateSteps(params.minScale, params.maxScale, params.scale)
        val newMaxOffset = max(0f, (parameterLists.size - stepCount).toFloat())

        chartParameters[id] = params.copy(
            minOffsetX = 0f,
            maxOffsetX = newMaxOffset,
            offset = if (autoScrollEnabled) newMaxOffset else params.offset.coerceIn(
                0f,
                newMaxOffset
            )
        )
    }

    LaunchedEffect(chartParameters[id]?.offset) {
        val params = chartParameters.getOrDefault(id, ChartParameters())
        autoScrollEnabled = params.offset >= params.maxOffsetX - 1f
    }

    val parameter = Parameter(
        id = id,
        stepCount = interpolateSteps(
            chartParameters[id]?.minScale ?: 1f,
            chartParameters[id]?.maxScale ?: 2f,
            chartParameters[id]?.scale ?: 1f
        ),
        label = ParametersLabel.ENCODER_READINGS,
        points = parameterLists
    )

    val onEvents: (Int, ParametersIntent) -> Unit = { chartId, event ->
        chartParameters[chartId] = when (event) {
            is ParametersIntent.ChangeScale -> chartParameters.getOrDefault(
                chartId, ChartParameters()
            ).copy(scale = event.scale)

            is ParametersIntent.ChangeOffset -> chartParameters.getOrDefault(
                chartId, ChartParameters()
            ).copy(offset = event.offset)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LineChart(
            parameter = parameter,
            chartParameters = chartParameters[id] ?: ChartParameters(),
            onEvents = onEvents
        )
    }
}
