package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.ParametersIntent
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.LiftParameters
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun LineCharts(
    parameters: List<LiftParameters>,
    chartParameters: ChartParameters,
    onEvents: (ParametersIntent) -> Unit,
) {
    val points = parameters.safeSubList(chartParameters.offset.toInt(), chartParameters.stepCount)
    if (points.isEmpty()) return

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

        onEvents(ParametersIntent.ChangeOffset(newOffsetX))
        onEvents(ParametersIntent.ChangeScale(newScale))
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
                onEvents(ParametersIntent.ChangeScale(1f))
                onEvents(ParametersIntent.ChangeOffset(chartParameters.maxOffsetX))
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
            DrawGraph(
                parameters = points,
                stepCounterXAxis = chartParameters.stepCount,
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
    }
}

fun <T> List<T>.safeSubList(start: Int, count: Int): List<T> {
    val safeStart = start.coerceAtLeast(0).coerceAtMost(size)
    val safeEnd = (start + count).coerceAtMost(size)
    return if (safeStart >= safeEnd) emptyList() else subList(safeStart, safeEnd)
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
                data = listOf(0, 1, 1, 1)
            ),
            LiftParameters(
                timeStamp = 123435L,
                timeMilliseconds = 80,
                frameId = 20,
                data = listOf(0, 2, 1, 1)
            ),
            LiftParameters(
                timeStamp = 123436L,
                timeMilliseconds = 80,
                frameId = 20,
                data = listOf(0, 1, 1, 0)
            ),
        )
    }

    fun generateNewLiftParameters(last: LiftParameters) = last.copy(
        timeStamp = last.timeStamp + 1,
        data = last.data.map { it + listOf(-1, 0, 1).random() }
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
            onEvents = onEvents
        )
    }
}