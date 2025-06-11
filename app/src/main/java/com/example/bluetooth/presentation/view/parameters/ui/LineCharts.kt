package com.example.bluetooth.presentation.view.parameters.ui

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.parameters.model.DataPoint
import com.example.bluetooth.presentation.view.parameters.model.DisplayValueWithColor
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import com.example.bluetooth.presentation.view.parameters.ui.chart.LineChartContent
import com.example.bluetooth.presentation.view.parameters.ui.tooltip.ChartValueTooltip
import com.example.bluetooth.presentation.view.parameters.viewmodel.ParametersEvents
import com.example.transfer.chart.domain.model.ChartConfig
import kotlinx.coroutines.delay
import kotlin.math.abs


@Composable
fun LineCharts(
    chartData: List<GraphSeries>,
    parameterDisplayData: ParameterDisplayData,
    chartConfig: ChartConfig,
    onEvents: (ParametersEvents) -> Unit,
) {
    if (chartData.isEmpty()) {
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

    var chartBoxSize by remember { mutableStateOf(IntSize.Zero) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var stepSizeX by remember { mutableFloatStateOf(0f) }
    val currentScale by rememberUpdatedState(chartConfig.scale)
    val currentOffset by rememberUpdatedState(chartConfig.offset)
    val currentMinOffsetX by rememberUpdatedState(chartConfig.minOffsetX)
    val currentMaxOffsetX by rememberUpdatedState(chartConfig.maxOffsetX)

    val onReset by rememberUpdatedState {
        onEvents(ParametersEvents.ChangeScale(1f))
        onEvents(ParametersEvents.ChangeOffset(currentMaxOffsetX))
    }


    val handleTransform: (Offset, Float) -> Unit = { pan, zoom ->
        val newScale = (currentScale * zoom)
            .coerceIn(chartConfig.minScale, chartConfig.maxScale)
        val newOffsetX = (currentOffset - pan.x / 10)
            .coerceIn(currentMinOffsetX, currentMaxOffsetX)

        onEvents(ParametersEvents.ChangeOffset(newOffsetX))
        onEvents(ParametersEvents.ChangeScale(newScale))
    }

    val handleTap: (Offset) -> Unit = run@{ tapPosition ->
        touchPosition = if (touchPosition != null) null else tapPosition

        val selectedIndex = if (touchPosition == null) null
        else {
            val referenceLine = chartData.firstOrNull { it.points.isNotEmpty() } ?: return@run

            val minX = referenceLine.points.minOf { it.xCoordinate }

            val tappedX = tapPosition.x / stepSizeX + minX

            val index = referenceLine.points
                .mapIndexed { idx, point -> idx to point.xCoordinate }
                .minByOrNull { (_, x) -> abs(x - tappedX) }
                ?.first ?: return@run
            index.coerceIn(0, referenceLine.points.lastIndex)
        }
        onEvents(ParametersEvents.Tap(selectedIndex))
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        Header(
            scale = chartConfig.scale,
            offset = chartConfig.offset,
            onReset = onReset
        )
        ChartDrawingSurface(
            chartData = chartData,
            touchPosition = touchPosition,
            parameterDisplayData = parameterDisplayData,
            stepCounterXAxis = chartConfig.stepCount,
            chartBoxSize = chartBoxSize,
            onBoxSizeChanged = { chartBoxSize = it },
            onStepSizeXChanged = { stepSizeX = it },
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
fun Header(
    scale: Float,
    offset: Float,
    onReset: () -> Unit
) {
    Column {
        ScaleOffsetInfo(scale = scale, offset = offset)
        Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сбросить масштаб и скролл")
        }
    }
}

@Composable
private fun ScaleOffsetInfo(scale: Float, offset: Float) {
    Text(
        text = "Масштаб: $scale | Смещение: $offset",
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
private fun ChartDrawingSurface(
    chartData: List<GraphSeries>,
    touchPosition: Offset?,
    parameterDisplayData: ParameterDisplayData,
    stepCounterXAxis: Int,
    chartBoxSize: IntSize,
    onBoxSizeChanged: (IntSize) -> Unit,
    onStepSizeXChanged: (Float) -> Unit,
    onTap: (Offset) -> Unit,
    onTransform: (Offset, Float) -> Unit,
    stepSizeX: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.onGloballyPositioned { onBoxSizeChanged(it.size) }
    ) {
        LineChartContent(
            chartData = chartData,
            stepCountXAxis = stepCounterXAxis,
            selectedIndex = parameterDisplayData.selectedIndex,
            onStepSizeXAxisChange = onStepSizeXChanged,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stepSizeX, chartData) {
                    detectTapGestures { tapPosition -> onTap(tapPosition) }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ -> onTransform(pan, zoom) }
                }
        )
        if (touchPosition != null) {
            ChartValueTooltip(
                touchPosition = touchPosition,
                values = parameterDisplayData,
                parentSize = chartBoxSize,
            )
        }
    }
}


@Preview
@Composable
fun LineChartPreview() {
    fun generateNewGraphPoints(oldList: List<GraphSeries>): List<GraphSeries> {
        val lastX = oldList.flatMap { it.points }.maxOfOrNull { it.xCoordinate }?.toInt() ?: 0
        val newX = (lastX + (0..1).random()).toFloat()

        return oldList.map { graphLine ->
            val newPoint = DataPoint(
                xCoordinate = newX,
                yCoordinate = (graphLine.points.lastOrNull()?.yCoordinate ?: 0f) + (-1..1).random()
            )
            graphLine.copy(points = graphLine.points + newPoint)
        }
    }

    fun interpolateSteps(
        minScale: Float,
        maxScale: Float,
        currentScale: Float,
        minScalePoint: Int,
        maxScalePoint: Int
    ): Int =
        (minScalePoint + ((currentScale - minScale) * (maxScalePoint - minScalePoint) /
                (maxScale - minScale))).toInt()

    fun List<GraphSeries>.filterVisibleRange(
        startX: Float,
        count: Int
    ): List<GraphSeries> {
        val endX = startX + count
        return map { graphLine ->
            graphLine.copy(
                points = graphLine.points
                    .filter { it.xCoordinate in startX..endX }
                    .sortedBy { it.xCoordinate }
            )
        }
    }

    val simpleChartData = remember {
        mutableStateOf(
            listOf(
                GraphSeries(
                    name = "ELEVATOR_SPEED",
                    points = List(10) { index ->
                        DataPoint(
                            index.toFloat(),
                            (index * 10).toFloat()
                        )
                    },
                    color = Color.Blue
                ),
                GraphSeries(
                    name = "ENCODER_READINGS",
                    points = List(10) { index ->
                        DataPoint(
                            index.toFloat(),
                            (index * 5).toFloat()
                        )
                    },
                    color = Color.Red
                ),
                GraphSeries(
                    name = "ENCODER_READINGS",
                    points = List(10) { index ->
                        DataPoint(
                            index.toFloat(),
                            (index).toFloat()
                        )
                    },
                    color = Color.Green
                ),
                GraphSeries(
                    name = "ENCODER_READINGS",
                    points = List(10) { index ->
                        DataPoint(
                            index.toFloat(),
                            (0).toFloat()
                        )
                    },
                    color = Color.Black
                ),
            )
        )
    }

    var chartConfig by remember { mutableStateOf(ChartConfig()) }

    val test = remember {
        mutableStateOf(
            ParameterDisplayData(
                selectedIndex = null,
                timestamp = 0L,
                timeMilliseconds = 0,
                parameters = mapOf(
                    "ELEVATOR_SPEED" to DisplayValueWithColor(32f, Color.Black),
                    "ENCODER_READINGS" to DisplayValueWithColor(3f, Color.Green),
                )
            )
        )

    }

    var autoScrollEnabled by remember { mutableStateOf(true) }


    val onEvents: (ParametersEvents) -> Unit = { event ->
        chartConfig = when (event) {
            is ParametersEvents.ChangeScale -> {
                val newScale = event.scale.coerceIn(
                    chartConfig.minScale,
                    chartConfig.maxScale
                )
                chartConfig.copy(scale = newScale)
            }

            is ParametersEvents.ChangeOffset -> {
                val newOffset = event.offset.coerceIn(
                    chartConfig.minOffsetX,
                    chartConfig.maxOffsetX
                )
                chartConfig.copy(offset = newOffset)
            }

            is ParametersEvents.Tap -> {
                test.value = test.value.copy(selectedIndex = event.selectedIndex)
                chartConfig
            }

        }
    }


    if (!LocalInspectionMode.current) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                simpleChartData.value = generateNewGraphPoints(simpleChartData.value)
            }
        }
    }

    LaunchedEffect(simpleChartData.value.flatMap { it.points }.size, chartConfig.scale) {
        val stepCount = interpolateSteps(
            minScale = chartConfig.minScale,
            maxScale = chartConfig.maxScale,
            currentScale = chartConfig.scale,
            minScalePoint = chartConfig.minScalePoint,
            maxScalePoint = chartConfig.maxScalePoint,
        )

        val minX = simpleChartData.value.flatMap { it.points }.minOfOrNull { it.xCoordinate } ?: 0f
        val maxX = simpleChartData.value.flatMap { it.points }.maxOfOrNull { it.xCoordinate } ?: 0f
        val maxOffsetX = (maxX - minX - stepCount)
            .coerceAtLeast(0f)

        chartConfig = chartConfig.copy(
            minOffsetX = 0f,
//            stepCount = stepCount,
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
            chartData = simpleChartData.value.filterVisibleRange(
                chartConfig.offset,
                chartConfig.stepCount
            ),
            parameterDisplayData = test.value,
            chartConfig = chartConfig,
            onEvents = onEvents,
        )
    }
}