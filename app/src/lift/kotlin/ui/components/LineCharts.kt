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
import kotlin.math.max
import kotlin.math.roundToInt

//todo нужно сделать подсказку для лучшей обработк
// вот а еще нужно будет сделать потом графики чтобы накладывались на друг друга
// ото есть проблема с количеством дискретнымых графиков

@Composable
fun LineCharts(
    parameters: List<LiftParameters>,
    chartParameters: ChartParameters,
    onEvents: (ParametersIntent) -> Unit,
) {
    if (parameters.isEmpty()) return
    val points = parameters.safeSubList(chartParameters.offset.toInt(), chartParameters.stepCount)

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
        onEvents(
            ParametersIntent.DataPointSelected(
                timeStamp = points[index].timeStamp,
                timeMilliseconds = points[index].timeMilliseconds
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        Header(chartParameters, onEvents)
        ChartCanvas(
            parameters = points,
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
        modifier = modifier
            .onGloballyPositioned {
                onBoxSizeChanged(it.size)
            }
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
                values = parameters[selectedIndex],
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
                if (stepSizeX > 0 && parameters.isNotEmpty()) {
                    val index =
                        (tapPosition.x / stepSizeX).roundToInt().coerceIn(parameters.indices)
                    onTap(tapPosition, index)
                }
            }
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                onTransform(pan, zoom)
            }
        }
}


fun <T> List<T>.safeSubList(start: Int, count: Int): List<T> {
    val safeStart = start.coerceAtLeast(0).coerceAtMost(size)
    val safeEnd = (start + count).coerceAtMost(size)
    return if (safeStart >= safeEnd) emptyList() else subList(safeStart, safeEnd).toList()
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

    fun generateNewLiftParameters(last: LiftParameters) = last.copy(
        timeStamp = last.timeStamp + 1,
        data = last.data.map {
            it.copy(value = it.value + listOf(-1, 0, 1).random())
        }
    )
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            parameterLists.add(
                generateNewLiftParameters(parameterLists.lastOrNull() ?: continue)
            )
        }
    }

    val minScalePoint = 300
    val maxScalePoint = 10

    fun interpolateSteps(minScale: Float, maxScale: Float, currentScale: Float): Int =
        (minScalePoint + ((currentScale - minScale) * (maxScalePoint - minScalePoint) / (maxScale - minScale))).toInt()

    var chartParameters by remember { mutableStateOf(ChartParameters()) }

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

            is ParametersIntent.DataPointSelected -> {
                chartParameters
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
        val newMaxOffset = max(0f, (parameterLists.size - stepCount).toFloat())

        chartParameters = chartParameters.copy(
            minOffsetX = 0f,
            stepCount = stepCount,
            maxOffsetX = newMaxOffset,
            offset = if (autoScrollEnabled) newMaxOffset else chartParameters.offset.coerceIn(
                0f,
                newMaxOffset
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
            parameters = parameterLists,
            chartParameters = chartParameters,
            onEvents = onEvents,
        )
    }
}