package com.psis.elimlift.presentation.view.parameters.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.psis.elimlift.presentation.view.parameters.model.Chart
import com.psis.elimlift.presentation.view.parameters.model.ParameterDisplayData
import com.psis.elimlift.presentation.view.parameters.ui.chart.LineChart
import com.psis.elimlift.presentation.view.parameters.ui.tooltip.ChartValueTooltip
import com.psis.elimlift.presentation.view.parameters.viewmodel.ParametersEvents
import com.psis.transfer.chart.domain.model.ChartConfig
import kotlin.math.abs
import kotlin.random.Random


@Composable
fun LineCharts(
    chartData: List<Chart>,
    touchPosition: Offset?,
    parameterDisplayData: ParameterDisplayData,
    chartConfig: ChartConfig,
    onEvent: (ParametersEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    val currentScale by rememberUpdatedState(chartConfig.scale)
    val currentOffset by rememberUpdatedState(chartConfig.offset)

    val currentMinOffsetX by rememberUpdatedState(chartConfig.minOffsetX)
    val currentMaxOffsetX by rememberUpdatedState(chartConfig.maxOffsetX)

    val onReset by rememberUpdatedState {
        onEvent(ParametersEvents.ChangeScale(1f))
        onEvent(ParametersEvents.ChangeOffset(currentMaxOffsetX))
    }

    val onTransformGesture =
        remember(currentOffset, currentScale, currentMinOffsetX, currentMaxOffsetX) {
            { pan: Offset, zoom: Float ->
                onEvent(
                    ParametersEvents.ChangeOffset(
                        offset = (currentOffset - pan.x / 10)
                            .coerceIn(currentMinOffsetX, currentMaxOffsetX)
                    )
                )
                onEvent(
                    ParametersEvents.ChangeScale(
                        scale = (currentScale * zoom)
                            .coerceIn(chartConfig.minScale, chartConfig.maxScale)
                    )
                )
            }
        }
    val onTapGesture = remember {
        { tapPosition: Offset ->
            onEvent(ParametersEvents.Tap(touchPosition = tapPosition))
        }
    }


    val updateChartBoxSize: (IntSize) -> Unit = { parentSize = it }
    val updateCanvasSize: (IntSize) -> Unit = {
        onEvent(ParametersEvents.ChangeCanvasSize(size = it))
    }

    Column(modifier = modifier) {
        Header(
            scale = chartConfig.scale,
            offset = chartConfig.offset,
            onReset = onReset
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned { updateChartBoxSize(it.size) }
                .chartGestures(  // ⬅️ теперь жесты обрабатываются на всём LineCharts
                    onTap = onTapGesture,
                    onTransform = onTransformGesture
                )
        ) {
            ChartsContent(
                chartData = chartData,
                parameterDisplayData = parameterDisplayData,
                onCanvasSizeChange = updateCanvasSize
            )

            touchPosition?.let {
                ChartValueTooltip(
                    touchPosition = it,
                    values = parameterDisplayData,
                    parentSize = parentSize
                )
            }
        }
    }
}

@Composable
private fun ChartsContent(
    chartData: List<Chart>,
    parameterDisplayData: ParameterDisplayData,
    onCanvasSizeChange: (IntSize) -> Unit
) {
    if (chartData.isEmpty()) {
        EmptyChart(Modifier.fillMaxSize())
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chartData) { chart ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    LineChart(
                        chartData = chart,
                        selectedIndex = parameterDisplayData.selectedIndex,
                        onCanvasSizeChange = onCanvasSizeChange,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}


fun Modifier.chartGestures(
    onTap: (Offset) -> Unit,
    onTransform: (pan: Offset, zoom: Float) -> Unit
): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        // 1) ждём касание (не требуем, чтобы оно было «не потреблено»)
        val down = awaitFirstDown(requireUnconsumed = false)
        var isTap = true
        var verticalAccum = 0f
        val touchSlop = viewConfiguration.touchSlop

        while (true) {
            // Берём событие в Main pass (достаточно здесь)
            val event = awaitPointerEvent(PointerEventPass.Main)
            val pointersDown = event.changes.count { it.pressed }
            val pan = event.calculatePan()
            val zoom = event.calculateZoom()

            val isMultiTouch = pointersDown > 1 || zoom != 1f
            val isHorizontalPan = abs(pan.x) > abs(pan.y)

            if (isMultiTouch || isHorizontalPan) {
                // Горизонтальная прокрутка графика или пинч-зум → забираем событие себе
                onTransform(pan, zoom)
                event.changes.forEach { it.consume() }
                isTap = false
            } else {
                // Вертикальный пан одним пальцем → отдаём LazyColumn
                verticalAccum += abs(pan.y)
                if (verticalAccum > touchSlop) isTap = false
                // НИЧЕГО не consume-им!
            }

            // Выход, когда все пальцы отпущены
            if (event.changes.none { it.pressed }) break
        }

        // Если не было существенного движения/зума — считаем это тапом
        if (isTap) onTap(down.position)
    }
}


@Composable
private fun EmptyChart(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Unspecified
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

@Preview(showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun LineChartsPreviewEmpty() {
    LineCharts(
        chartData = emptyList(),
        touchPosition = null,
        parameterDisplayData = ParameterDisplayData(
            selectedIndex = 0,
            timestamp = 0,
            timeMilliseconds = 0,
            parameters = emptyMap()
        ),
        chartConfig = ChartConfig(scale = 1f),
        onEvent = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LineChartsPreviewMultiChart() {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }


    val fakeChartData = remember(canvasSize) { ChartBuilder(canvasSize).charts() }
    val fakeParameterDisplayData = ParameterDisplayData(
        selectedIndex = 0,
        timestamp = 0,
        timeMilliseconds = 0,
        parameters = emptyMap()
    )

    var fakeChartConfig by remember {
        mutableStateOf(
            ChartConfig(scale = 2f)
        )
    }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }


    val onEvents: (ParametersEvents) -> Unit = { event ->
        when (event) {
            is ParametersEvents.ChangeCanvasSize -> canvasSize = event.size
            is ParametersEvents.ChangeOffset -> fakeChartConfig =
                fakeChartConfig.copy(offset = event.offset)

            is ParametersEvents.ChangeScale -> fakeChartConfig =
                fakeChartConfig.copy(scale = event.scale)

            is ParametersEvents.Tap -> touchPosition = event.touchPosition
        }
    }

    LineCharts(
        chartData = fakeChartData,
        touchPosition = touchPosition,
        parameterDisplayData = fakeParameterDisplayData,
        chartConfig = fakeChartConfig,
        onEvent = onEvents
    )
}


class ChartBuilder(private val canvasSize: IntSize) {
    private val random = Random(12)

    fun points(
        minXChart: Float = 0f,
        maxXChart: Float = 1f,
        minYChart: Float = 0f,
        maxYChart: Float = 1f,
        generateXCoordinates: () -> List<Float> = { generateXCoordinatesSeries(random) },
        generateYCoordinate: () -> Float = { random.nextDouble(0.0, 1.0).toFloat() }
    ): List<Offset> {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return listOf(Offset.Zero)

        val stepX = canvasSize.width / (maxXChart - minXChart)
        val stepY = canvasSize.height / (maxYChart - minYChart)
        val height = canvasSize.height.toFloat()

        return generateXCoordinates()
            .map { x -> Offset(x * stepX, height - generateYCoordinate() * stepY) }
            .sortedBy { it.x }
    }

    fun chart(
        name: String = "Sample Chart",
        color: Color = Color.Blue,
        minValue: Float = Float.MIN_VALUE,
        maxValue: Float = Float.MAX_VALUE
    ): Chart = Chart(name, points(), color, minValue, maxValue)

    fun charts(
        size: Int = 3,
        chartFactory: (String, List<Offset>) -> Chart = { name, pts ->
            Chart(name, pts, Color.Blue, Float.MIN_VALUE, Float.MAX_VALUE)
        }
    ): List<Chart> {
        val pts = points()
        return List(size) { index -> chartFactory("Sample Chart $index", pts) }
    }

    private fun generateXCoordinatesSeries(
        random: Random,
        minX: Float = 0f,
        maxX: Float = 1f,
        stepMin: Float = 0f,
        stepMax: Float = 1f,
        maxCount: Int = 100,
    ): List<Float> = buildList {
        var current = minX
        add(current)

        while (current < maxX && size < maxCount) {
            val step = random.nextDouble(stepMin.toDouble(), stepMax.toDouble()).toFloat()
            current += step
            if (step == 0f) add(current) else if (current <= maxX) add(current)
        }
    }
}



