package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.extensions.isNotNull
import com.example.bluetooth.presentation.ParametersIntent
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParameterLabel
import com.example.transfer.model.ParametersLabel
import com.example.transfer.model.Test
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

//todo графики занимают много место нужно наложить их

@Composable
fun LineCharts(
    parameters: List<LiftParameters>,
    chartParameters: ChartParameters,
    onEvents: (ParametersIntent) -> Unit,
) {
    if (parameters.isEmpty()) return
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chartBoxSize by remember { mutableStateOf(IntSize.Zero) }

    var stepSizeX by remember { mutableFloatStateOf(0f) }
    var stepSizeY by remember { mutableFloatStateOf(0f) }

    val currentScale by rememberUpdatedState(chartParameters.scale)
    val currentOffset by rememberUpdatedState(chartParameters.offset)
    val currentMinOffsetX by rememberUpdatedState(chartParameters.minOffsetX)
    val currentMaxOffsetX by rememberUpdatedState(chartParameters.maxOffsetX)

    val handleTransform: (Offset, Float) -> Unit = { pan, zoom ->
        val newScale = (currentScale * zoom)
            .coerceIn(chartParameters.minScale, chartParameters.maxScale)
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
        Header(chartParameters, onEvents)
        ChartCanvas(
            parameters = parameters,
            stepCounterXAxis = chartParameters.stepCount,
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
private fun ChartCanvas(
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
            stepCounterXAxis = stepCounterXAxis,
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
            lineColors = listOf(Color.Red, Color.Blue)
        )
        if (touchPosition != null && selectedIndex != null) {
            Tooltip(
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
    this
        .pointerInput(stepSizeX, parameters) {
            detectTapGestures { tapPosition ->
                if (stepSizeX > 0) {
                    val baseTimeMs = parameters.calculateBaseTime()
                    val tapTime = (tapPosition.x / stepSizeX) * 1000

                    val nearestIndex = parameters
                        .mapIndexed { index, param ->
                            index to ((param.timeStamp * 1000 + param.timeMilliseconds) - baseTimeMs)
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
fun Tooltip(
    position: Offset,
    values: LiftParameters,
    parentSize: IntSize,
    lineColors: List<Color>,
    backgroundColor: Color = Color(0xCC333333)
) {
    val density = LocalDensity.current
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }

    val marginPx = with(density) { 12.dp.toPx() }
    val pointerOffsetPx = with(density) { 16.dp.toPx() }

    val rawX = position.x + pointerOffsetPx
    val rawY = position.y - tooltipSize.height - pointerOffsetPx

    val clampedX = rawX.coerceIn(marginPx, parentSize.width - tooltipSize.width - marginPx)
    val clampedY = rawY.coerceIn(marginPx, parentSize.height - tooltipSize.height - marginPx)

    val xOffsetDp = with(density) { clampedX.toDp() }
    val yOffsetDp = with(density) { clampedY.toDp() }

    val formattedTime = remember(values.timeStamp, values.timeMilliseconds) {
        val millis = values.timeStamp * 1000 + values.timeMilliseconds
        val date = Date(millis)
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(date)
    }

    Box(
        modifier = Modifier
            .offset(x = xOffsetDp, y = yOffsetDp)
            .onGloballyPositioned {
                tooltipSize = it.size
            }
            .background(backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = formattedTime, fontSize = 12.sp, color = Color.White)
            Spacer(Modifier.height(4.dp))
            values.data.forEachIndexed { index, (label, value) ->
                TooltipRow(
                    label = label,
                    value = value,
                    indicatorColor = lineColors.getOrNull(index) ?: Color.Gray
                )
            }
        }
    }
}

@Composable
private fun TooltipRow(
    label: ParameterLabel,
    value: Int,
    indicatorColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(indicatorColor, shape = CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text("${label.value}: $value", fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun Header(chartParameters: ChartParameters, onEvents: (ParametersIntent) -> Unit) {
    Column {
        Text(
            text = "Масштаб: ${chartParameters.scale} | Смещение: ${chartParameters.offset}",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(8.dp)
        )
        Button(
            onClick = {
                onEvents(ParametersIntent.ChangeScale(1f))
                onEvents(ParametersIntent.ChangeOffset(chartParameters.maxOffsetX))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сбросить масштаб и скролл")
        }
    }
}


@Preview
@Composable
fun LineChartPreview() {
    val parameterLists = remember {
        mutableStateListOf(
            LiftParameters(
                timeStamp = 123434L,
                timeMilliseconds = 80,
                frameId = 20,
                data = listOf(
                    Test(ParametersLabel.ENCODER_READINGS, 0),
                    Test(ParametersLabel.ENCODER_FREQUENCY, 1),
                    Test(ParametersLabel.ELEVATOR_SPEED, 1),
                    Test(ParametersLabel.FRAME_ID, 1)
                )
            ),
            LiftParameters(
                timeStamp = 123435L,
                timeMilliseconds = 80,
                frameId = 20,
                data = listOf(
                    Test(ParametersLabel.ENCODER_READINGS, 0),
                    Test(ParametersLabel.ENCODER_FREQUENCY, 2),
                    Test(ParametersLabel.ELEVATOR_SPEED, 1),
                    Test(ParametersLabel.FRAME_ID, 1)
                )
            ),
            LiftParameters(
                timeStamp = 123436L,
                timeMilliseconds = 80,
                frameId = 20,
                data = listOf(
                    Test(ParametersLabel.ENCODER_READINGS, 0),
                    Test(ParametersLabel.ENCODER_FREQUENCY, 1),
                    Test(ParametersLabel.ELEVATOR_SPEED, 1),
                    Test(ParametersLabel.FRAME_ID, 0)
                )
            ),
        )
    }
    fun generateNewLiftParameters(last: LiftParameters): LiftParameters {
        val deltaSeconds = (0..1).random()
        val newTimeStamp = last.timeStamp + deltaSeconds

        val newMilliseconds = if (deltaSeconds == 0) (last.timeMilliseconds..999).random()
        else (0..999).random()

        return last.copy(
            timeStamp = newTimeStamp,
            timeMilliseconds = newMilliseconds,
            data = last.data.map {
                it.copy(value = it.value + listOf(-1, 0, 1).random())
            }
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            parameterLists.add(
                generateNewLiftParameters(parameterLists.lastOrNull() ?: continue)
            )
        }
    }

    val minScalePoint = 300
    val maxScalePoint = 3

    fun interpolateSteps(minScale: Float, maxScale: Float, currentScale: Float): Int =
        (minScalePoint + ((currentScale - minScale) * (maxScalePoint - minScalePoint) / (maxScale - minScale))).toInt()

    var chartParameters by remember { mutableStateOf(ChartParameters()) }

    val points = parameterLists.filterByTimestampRange(
        chartParameters.offset,
        chartParameters.stepCount
    )
    val onEvents: (ParametersIntent) -> Unit = { event ->
        chartParameters = when (event) {
            is ParametersIntent.ChangeScale -> {
                val newScale = event.scale.coerceIn(
                    chartParameters.minScale,
                    chartParameters.maxScale
                )
                chartParameters.copy(scale = newScale)
            }

            is ParametersIntent.ChangeOffset -> {
                val newOffset = event.offset.coerceIn(
                    chartParameters.minOffsetX,
                    chartParameters.maxOffsetX
                )
                chartParameters.copy(offset = newOffset)
            }
        }
    }


    var autoScrollEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(parameterLists.size, chartParameters.scale) {
        val stepCount = interpolateSteps(
            chartParameters.minScale,
            chartParameters.maxScale,
            chartParameters.scale
        )

        val minTimeTest = parameterLists.minOf { it.timeStamp }
        val maxTimeTest = parameterLists.maxOf { it.timeStamp }
        val maxOffsetX = (maxTimeTest- minTimeTest - chartParameters.stepCount).toFloat()
            .coerceAtLeast(0f)

        chartParameters = chartParameters.copy(
            minOffsetX = 0f,
            stepCount = stepCount,
            maxOffsetX = maxOffsetX,
            offset = if (autoScrollEnabled) maxOffsetX else chartParameters.offset.coerceIn(
                0f,
                maxOffsetX
            )
        )
    }

    LaunchedEffect(chartParameters) {
        autoScrollEnabled = chartParameters.offset >= chartParameters.maxOffsetX - 1f
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LineCharts(
            parameters = points,
            chartParameters = chartParameters,
            onEvents = onEvents,
        )
    }
}

fun List<LiftParameters>.filterByTimestampRange(offset: Float, count: Int): List<LiftParameters> {
    if (isEmpty()) return emptyList()
    val timeResolver = { param: LiftParameters ->
        param.timeStamp + param.timeMilliseconds / 1000f
    }

    val rangeStart = minOf { it.timeStamp } + offset
    val rangeEnd = (rangeStart + count).coerceAtMost(maxOf { it.timeStamp }.toFloat())

    return this
        .asSequence()
        .filter {
            val time = timeResolver(it)
            time in rangeStart..rangeEnd
        }
        .sortedWith(
            compareBy(
                LiftParameters::timeStamp,
                LiftParameters::timeMilliseconds
            )
        )
        .toList()
}
