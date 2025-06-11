package com.example.bluetooth.presentation.view.parameters.viewmodel

import androidx.compose.ui.graphics.Color
import com.example.bluetooth.presentation.view.parameters.model.DataPoint
import com.example.bluetooth.presentation.view.parameters.model.DisplayValueWithColor
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import com.example.transfer.chart.domain.model.ChartConfig

object ParametersStateDefaults {
    fun getDefault() = ParametersState(
        time = "",
        chartData = listOf(
            GraphSeries(
                name = "TEST_DATA",
                points = List(10) { index ->
                    DataPoint(
                        index.toFloat(),
                        (index * 10).toFloat()
                    )
                },
                color = Color.Blue
            ),
            GraphSeries(
                name = "TEST_DATA2",
                points = List(10) { index ->
                    DataPoint(
                        index.toFloat(),
                        (index * 5).toFloat()
                    )
                },
                color = Color.Red
            ),
        ),
        popData = ParameterDisplayData(
            selectedIndex = null,
            timestamp = 0L,
            timeMilliseconds = 0,
            parameters = mapOf(
                "ELEVATOR_SPEED" to DisplayValueWithColor(32f, Color.Black),
                "ENCODER_READINGS" to DisplayValueWithColor(3f, Color.Green),
            )
        ),
        chartConfig = ChartConfig(),
    )
}