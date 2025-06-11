package com.example.transfer.chart.data.mapper

import com.example.transfer.chart.domain.model.SignalColor
import com.example.transfer.chart.domain.model.DisplayValueWithColor
import com.example.transfer.chart.domain.model.GraphSeries
import com.example.transfer.chart.domain.model.ParameterDisplayData
import javax.inject.Inject

class ParameterDisplayDataMapper @Inject constructor() {
    fun map(
        graphData: List<GraphSeries>,
        selectedIndex: Int?
    ): ParameterDisplayData {
        if (selectedIndex == null) return ParameterDisplayData()

        val parameters = mutableMapOf<String, DisplayValueWithColor>()
        var timestamp = 0L
        var timeMillis = 0

        for (series in graphData) {
            val point = series.points.getOrNull(selectedIndex) ?: continue

            if (timestamp == 0L) {
                timestamp = point.timestamp
                timeMillis = point.timeMilliseconds
            }

            val value = DisplayValueWithColor(
                value = point.yCoordinate,
                color = series.color ?: DEFAULT_COLOR
            )
            parameters[series.name] = value
        }

        return ParameterDisplayData(
            selectedIndex = selectedIndex,
            timestamp = timestamp,
            timeMilliseconds = timeMillis,
            parameters = parameters
        )
    }

    companion object {
        private val DEFAULT_COLOR = SignalColor(0, 0, 0)
    }
}