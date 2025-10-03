package com.psis.transfer.chart.data.mapper

import com.psis.transfer.chart.domain.model.SignalColor
import com.psis.transfer.chart.domain.model.DisplayValueWithColor
import com.psis.transfer.chart.domain.model.GraphSeries
import com.psis.transfer.chart.domain.model.ParameterDisplayData
import javax.inject.Inject

class ParameterDisplayDataMapper @Inject constructor() {
    fun map(
        graphData: List<GraphSeries>,
        selectedIndex: Int?
    ): ParameterDisplayData {
        if (selectedIndex == null) return ParameterDisplayData()

        val dataPoints = graphData.mapNotNull { series ->
            val point = series.points.getOrNull(selectedIndex) ?: return@mapNotNull null
            series.name to DisplayValueWithColor(
                value = point.yCoordinate,
                color = series.color ?: DEFAULT_COLOR
            ) to point
        }

        val parameters = dataPoints.associate { it.first }
        val referencePoint = dataPoints.firstOrNull()?.second

        return ParameterDisplayData(
            selectedIndex = selectedIndex,
            timestamp = referencePoint?.timestamp ?: 0L,
            timeMilliseconds = referencePoint?.timeMilliseconds ?: 0,
            parameters = parameters
        )
    }

    companion object {
        private val DEFAULT_COLOR = SignalColor(0, 0, 0)
    }
}