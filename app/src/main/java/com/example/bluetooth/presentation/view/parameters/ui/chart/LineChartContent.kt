package com.example.bluetooth.presentation.view.parameters.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.parameters.model.DataPoint
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.util.extractLatestValues


@Composable
fun LineChartContent(
    chartData: List<GraphSeries>,
    stepCountXAxis: Int,
    selectedIndex: Int?,
    onStepSizeXAxisChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    style: ChartStyle = ChartStyle()
) {
    if (chartData.isEmpty()) {
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

    val stableValues = remember(chartData) {
        extractLatestValues(chartData)
    }

    val stableFormatter = remember(style) { style.valueFormatter }

    Row(modifier = modifier) {
        LineChartCanvas(
            chartData = chartData,
            stepCountXAxis = stepCountXAxis,
            selectedIndex = selectedIndex,
            onStepSizeXAxisChange = onStepSizeXAxisChange,
            style = style,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        ValuesPanel(
            values = stableValues,
            valueFormatter = stableFormatter,
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun LineChartContentPreview() {
    val simpleChartData = remember {
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
    }

    LineChartContent(
        chartData = simpleChartData,
        stepCountXAxis = 10,
        selectedIndex = 5,
        onStepSizeXAxisChange = {},
        modifier = Modifier.fillMaxSize()
    )
}