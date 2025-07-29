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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import com.example.bluetooth.presentation.view.parameters.ui.chart.LineChartContent
import com.example.bluetooth.presentation.view.parameters.ui.tooltip.ChartValueTooltip
import com.example.bluetooth.presentation.view.parameters.viewmodel.ParametersEvents
import com.example.transfer.chart.domain.model.ChartConfig


@Composable
fun LineCharts(
    chartData: List<GraphSeries>,
    touchPosition: Offset?,
    parameterDisplayData: ParameterDisplayData,
    chartConfig: ChartConfig,
    onEvents: (ParametersEvents) -> Unit,
) {
    if (chartData.isEmpty()) {
        EmptyChart()
        return
    }

    var chartBoxSize by remember { mutableStateOf(IntSize.Zero) }
    val currentScale by rememberUpdatedState(chartConfig.scale)
    val currentOffset by rememberUpdatedState(chartConfig.offset)
    val currentMinOffsetX by rememberUpdatedState(chartConfig.minOffsetX)
    val currentMaxOffsetX by rememberUpdatedState(chartConfig.maxOffsetX)

    val onReset by rememberUpdatedState {
        onEvents(ParametersEvents.ChangeScale(1f))
        onEvents(ParametersEvents.ChangeOffset(currentMaxOffsetX))
    }

    val handleTransform =
        remember(currentOffset, currentScale, currentMinOffsetX, currentMaxOffsetX) {
            { pan: Offset, zoom: Float ->
                onEvents(
                    ParametersEvents.ChangeOffset(
                        offset = (currentOffset - pan.x / 10)
                            .coerceIn(currentMinOffsetX, currentMaxOffsetX)
                    )
                )
                onEvents(
                    ParametersEvents.ChangeScale(
                        scale = (currentScale * zoom)
                            .coerceIn(chartConfig.minScale, chartConfig.maxScale)
                    )
                )
            }
        }
    val onBoxSizeChanged: (IntSize) -> Unit = { chartBoxSize = it }
    val onCanvasSizeChange: (IntSize) -> Unit = {
        onEvents(ParametersEvents.ChangeCanvasSize(size = it))
    }

    val handleTap = remember {
        { tapPosition: Offset ->
            onEvents(ParametersEvents.Tap(touchPosition = tapPosition))
        }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned { onBoxSizeChanged(it.size) }
        ) {
            LineChartContent(
                chartData = chartData,
                selectedIndex = parameterDisplayData.selectedIndex,
                onCanvasSizeChange = onCanvasSizeChange,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = handleTap)
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ -> handleTransform(pan, zoom) }
                    }
            )
            touchPosition?.let {
                ChartValueTooltip(
                    touchPosition = it,
                    values = parameterDisplayData,
                    parentSize = chartBoxSize
                )
            }
        }
    }
}

@Composable
private fun EmptyChart() {
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