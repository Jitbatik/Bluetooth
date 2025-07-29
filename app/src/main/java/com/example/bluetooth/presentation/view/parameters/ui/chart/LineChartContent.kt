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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.util.extractLatestValues


@Composable
fun LineChartContent(
    chartData: List<GraphSeries>,
    selectedIndex: Int?,
    onCanvasSizeChange: (IntSize) -> Unit,
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
            selectedIndex = selectedIndex,
            onCanvasSizeChange = onCanvasSizeChange,
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
    val canvasSize = IntSize(width = 700, height = 2100)

    val chartData = remember(canvasSize) {
        generateFakeChartData(
            canvasSize = canvasSize,
            seriesCount = 2
        )
    }

    LineChartContent(
        chartData = chartData,
        selectedIndex = 5,
        onCanvasSizeChange = {},
        modifier = Modifier
            .fillMaxSize()
    )
}
