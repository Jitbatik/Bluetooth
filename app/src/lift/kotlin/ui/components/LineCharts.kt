package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import co.yml.charts.common.extensions.isNotNull
import com.example.bluetooth.presentation.ParametersIntent
import com.example.transfer.model.ChartConfig
import com.example.transfer.model.LiftParameterType
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParameterData
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun LineCharts(
    parameters: List<LiftParameters>,
    chartConfig: ChartConfig,
    onEvents: (ParametersIntent) -> Unit,
) {
    if (parameters.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
        return
    }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chartBoxSize by remember { mutableStateOf(IntSize.Zero) }

    var stepSizeX by remember { mutableFloatStateOf(0f) }
    var stepSizeY by remember { mutableFloatStateOf(0f) }

    val currentScale by rememberUpdatedState(chartConfig.scale)
    val currentOffset by rememberUpdatedState(chartConfig.offset)
    val currentMinOffsetX by rememberUpdatedState(chartConfig.minOffsetX)
    val currentMaxOffsetX by rememberUpdatedState(chartConfig.maxOffsetX)

    val handleTransform: (Offset, Float) -> Unit = { pan, zoom ->
        val newScale = (currentScale * zoom)
            .coerceIn(chartConfig.minScale, chartConfig.maxScale)
        val newOffsetX = (currentOffset - pan.x / 10)
            .coerceIn(currentMinOffsetX, currentMaxOffsetX)

        onEvents(ParametersIntent.ChangeOffset(newOffsetX))
        onEvents(ParametersIntent.ChangeScale(newScale))
    }

    val handleTap: (Offset, Int) -> Unit = { pos, index ->
        touchPosition = if (touchPosition.isNotNull()) null else pos
        selectedIndex = if (selectedIndex.isNotNull()) null else index
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        Header(chartConfig, onEvents)
        ChartDrawingSurface(
            parameters = parameters,
            stepCounterXAxis = chartConfig.stepCount,
            chartBoxSize = chartBoxSize,
            selectedIndex = selectedIndex,
            touchPosition = touchPosition,
            onBoxSizeChanged = { chartBoxSize = it },
            onStepSizeXChanged = { stepSizeX = it },
            onStepSizeYChanged = { stepSizeY = it },
            onTap = handleTap,
            onTransform = handleTransform,
            stepSizeX = stepSizeX,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun ChartDrawingSurface(
    parameters: List<LiftParameters>,
    stepCounterXAxis: Int,
    chartBoxSize: IntSize,
    selectedIndex: Int?,
    touchPosition: Offset?,
    onBoxSizeChanged: (IntSize) -> Unit,
    onStepSizeXChanged: (Float) -> Unit,
    onStepSizeYChanged: (Float) -> Unit,
    onTap: (Offset, Int) -> Unit,
    onTransform: (Offset, Float) -> Unit,
    stepSizeX: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.onGloballyPositioned { onBoxSizeChanged(it.size) }
    ) {
        DrawGraph(
            parameters = parameters,
            stepCountXAxis = stepCounterXAxis,
            selectedIndex = selectedIndex,
            onStepSizeXAxisChange = onStepSizeXChanged,
            onStepSizeYAxisChange = onStepSizeYChanged,
            modifier = Modifier
                .fillMaxSize()
                .detectChartGestures(
                    stepSizeX = stepSizeX,
                    parameters = parameters,
                    onTap = onTap,
                    onTransform = onTransform
                ),
            lineColors = mapOf(
                LiftParameterType.ENCODER_FREQUENCY to Color.Red,
                LiftParameterType.ENCODER_READINGS to Color.Blue
            ),
        )
        if (touchPosition != null && selectedIndex != null) {
            PointDetails(
                position = touchPosition,
                values = parameters[selectedIndex.coerceIn(0, parameters.size - 1)],
                parentSize = chartBoxSize,
                lineColors = listOf(Color.Red, Color.Blue)
            )
        }
    }
}


fun Modifier.detectChartGestures(
    stepSizeX: Float,
    parameters: List<LiftParameters>,
    onTap: (Offset, Int) -> Unit,
    onTransform: (Offset, Float) -> Unit
): Modifier = composed {
    if (parameters.isEmpty()) return@composed this

    this
        .pointerInput(stepSizeX, parameters) {
            detectTapGestures { tapPosition ->
                if (stepSizeX > 0) {
                    val baseTimeMs = parameters.calculateBaseTime()
                    val tapTime = (tapPosition.x / stepSizeX) * 1000

                    val nearestIndex = parameters
                        .mapIndexed { index, param ->
                            index to ((param.timestamp * 1000 + param.timeMilliseconds) - baseTimeMs)
                        }
                        .minByOrNull { (_, timeMs) -> abs(timeMs - tapTime) }
                        ?.first ?: 0

                    val safeIndex = nearestIndex.coerceIn(0, parameters.size - 1)
                    onTap(tapPosition, safeIndex)
                }
            }
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                onTransform(pan, zoom)
            }
        }
}

@Composable
private fun Header(chartConfig: ChartConfig, onEvents: (ParametersIntent) -> Unit) {
    Column {
        Text(
            text = "Масштаб: ${chartConfig.scale} | Смещение: ${chartConfig.offset}",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(8.dp)
        )
        Button(
            onClick = {
                onEvents(ParametersIntent.ChangeScale(1f))
                onEvents(ParametersIntent.ChangeOffset(chartConfig.maxOffsetX))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сбросить масштаб и скролл")
        }
    }
}

fun List<LiftParameters>.calculateBaseTime(): Long =
    minOfOrNull { it.timestamp * 1000L + it.timeMilliseconds } ?: 0L


@Preview
@Composable
fun LineChartPreview() {
    val parameterLists = remember {
        mutableStateListOf(
            LiftParameters(
                timestamp = 123434L,
                timeMilliseconds = 80,
                frameId = 20,
                parameters = listOf(
                    ParameterData(LiftParameterType.ENCODER_READINGS, 0),
                    ParameterData(LiftParameterType.ENCODER_FREQUENCY, 1),
                    ParameterData(LiftParameterType.ELEVATOR_SPEED, 1),
                    ParameterData(LiftParameterType.FRAME_ID, 1)
                )
            ),
            LiftParameters(
                timestamp = 123435L,
                timeMilliseconds = 80,
                frameId = 20,
                parameters = listOf(
                    ParameterData(LiftParameterType.ENCODER_READINGS, 0),
                    ParameterData(LiftParameterType.ENCODER_FREQUENCY, 2),
                    ParameterData(LiftParameterType.ELEVATOR_SPEED, 1),
                    ParameterData(LiftParameterType.FRAME_ID, 1)
                )
            ),
            LiftParameters(
                timestamp = 123436L,
                timeMilliseconds = 80,
                frameId = 20,
                parameters = listOf(
                    ParameterData(LiftParameterType.ENCODER_READINGS, 0),
                    ParameterData(LiftParameterType.ENCODER_FREQUENCY, 1),
                    ParameterData(LiftParameterType.ELEVATOR_SPEED, 1),
                    ParameterData(LiftParameterType.FRAME_ID, 0)
                )
            ),
        )
    }

    fun generateNewLiftParameters(last: LiftParameters): LiftParameters {
        val deltaSeconds = (0..1).random()
        val newTimeStamp = last.timestamp + deltaSeconds

        val newMilliseconds = when {
            deltaSeconds == 0 -> (last.timeMilliseconds..999).random()
            else -> (0..999).random()
        }.coerceIn(0, 999)

        return last.copy(
            timestamp = newTimeStamp,
            timeMilliseconds = newMilliseconds,
            parameters = last.parameters.map {
                it.copy(value = (it.value + (-1..1).random()).coerceAtLeast(0))
            }
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            parameterLists.add(
                generateNewLiftParameters(parameterLists.lastOrNull() ?: continue)
            )
        }
    }

    val minScalePoint = 300
    val maxScalePoint = 5

    fun interpolateSteps(minScale: Float, maxScale: Float, currentScale: Float): Int =
        (minScalePoint + ((currentScale - minScale) * (maxScalePoint - minScalePoint) / (maxScale - minScale))).toInt()

    var chartConfig by remember { mutableStateOf(ChartConfig()) }

    val points = parameterLists.filterByTimestampRange(
        chartConfig.offset,
        chartConfig.stepCount
    )
    val onEvents: (ParametersIntent) -> Unit = { event ->
        chartConfig = when (event) {
            is ParametersIntent.ChangeScale -> {
                val newScale = event.scale.coerceIn(
                    chartConfig.minScale,
                    chartConfig.maxScale
                )
                chartConfig.copy(scale = newScale)
            }

            is ParametersIntent.ChangeOffset -> {
                val newOffset = event.offset.coerceIn(
                    chartConfig.minOffsetX,
                    chartConfig.maxOffsetX
                )
                chartConfig.copy(offset = newOffset)
            }
        }
    }


    var autoScrollEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(parameterLists.size, chartConfig.scale) {
        val stepCount = interpolateSteps(
            chartConfig.minScale,
            chartConfig.maxScale,
            chartConfig.scale
        )

        val minTimeTest = parameterLists.minOf { it.timestamp }
        val maxTimeTest = parameterLists.maxOf { it.timestamp }
        val maxOffsetX = (maxTimeTest - minTimeTest - chartConfig.stepCount).toFloat()
            .coerceAtLeast(0f)

        chartConfig = chartConfig.copy(
            minOffsetX = 0f,
            stepCount = stepCount,
            maxOffsetX = maxOffsetX,
            offset = if (autoScrollEnabled) maxOffsetX else chartConfig.offset.coerceIn(
                0f,
                maxOffsetX
            )
        )
    }

    LaunchedEffect(chartConfig) {
        autoScrollEnabled = chartConfig.offset >= chartConfig.maxOffsetX - 1f
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LineCharts(
            parameters = points,
            chartConfig = chartConfig,
            onEvents = onEvents,
        )
    }
}

fun List<LiftParameters>.filterByTimestampRange(offset: Float, count: Int): List<LiftParameters> {
    if (isEmpty()) return emptyList()

    val timeResolver = { param: LiftParameters -> param.timestamp + param.timeMilliseconds / 1000f }
    val minTimestamp = minOf { it.timestamp }
    val maxTimestamp = maxOf { it.timestamp }

    val rangeStart = minTimestamp + offset
    val rangeEnd = (rangeStart + count).coerceAtMost(maxTimestamp.toFloat())

    return asSequence()
        .filter { param ->
            val time = timeResolver(param)
            time in rangeStart..rangeEnd
        }
        .sortedWith(compareBy(LiftParameters::timestamp, LiftParameters::timeMilliseconds))
        .toList()
}
