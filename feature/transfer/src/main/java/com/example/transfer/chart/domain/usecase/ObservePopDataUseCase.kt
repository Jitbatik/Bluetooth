package com.example.transfer.chart.domain.usecase

import com.example.transfer.chart.domain.model.DisplayValueWithColor
import com.example.transfer.chart.domain.model.GraphSeries
import com.example.transfer.chart.domain.model.ParameterDisplayData
import com.example.transfer.chart.domain.model.SignalColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObservePopDataUseCase @Inject constructor() {
    operator fun invoke(
        temp: Flow<List<GraphSeries>>,
        selectedIndex: MutableStateFlow<Int?>
    ): Flow<ParameterDisplayData> {
        return combine(temp, selectedIndex) { data, index ->
            map(data, index)
        }
    }


    private fun map(
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