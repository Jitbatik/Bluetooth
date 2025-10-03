package com.psis.elimlift.presentation.view.parameters.ui.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.psis.elimlift.presentation.view.parameters.model.Chart
import com.psis.elimlift.presentation.view.parameters.ui.ChartBuilder

@Composable
fun LineChart(
    chartData: Chart,
    selectedIndex: Int?,
    onCanvasSizeChange: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
    style: ChartStyle = ChartStyle(),
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ChartTitle(
                chartData.name,
                titleStyle,
                modifier = Modifier.padding(8.dp)
            )
            RangeLabel(
                minValue = chartData.minValue,
                maxValue = chartData.maxValue,
                color = chartData.color,
                style = titleStyle,
                modifier = Modifier.padding(8.dp)
            )
        }
        ChartBody(
            chartData = chartData,
            selectedIndex = selectedIndex,
            style = style,
            onCanvasSizeChange = onCanvasSizeChange
        )
    }
}

@Composable
private fun ChartTitle(
    name: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
//            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name, style = style, color = Color.Gray)
    }
}

@Composable
private fun ChartBody(
    chartData: Chart,
    selectedIndex: Int?,
    style: ChartStyle,
    onCanvasSizeChange: (IntSize) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LineChartCanvas(
            points = chartData.points,
            color = chartData.color,
            selectedIndex = selectedIndex,
            onCanvasSizeChange = onCanvasSizeChange,
            style = style,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
//        RangeLabel(
//            minValue = chartData.minValue,
//            maxValue = chartData.maxValue,
//            color = chartData.color,
//            modifier = Modifier.padding(4.dp)
//        )
    }
}


@Preview(showBackground = true)
@Composable
private fun LineChartContentPreview() {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val chart = remember(canvasSize) { ChartBuilder(canvasSize).chart() }

    LineChart(
        chartData = chart,
        selectedIndex = 5,
        onCanvasSizeChange = { canvasSize = it },
        modifier = Modifier
            .fillMaxSize()
    )
}