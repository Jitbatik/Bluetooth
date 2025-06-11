package com.example.transfer.chart.domain.usecase

import com.example.transfer.chart.data.mapper.ParameterDisplayDataMapper
import com.example.transfer.chart.domain.ByteDataToGraphSeriesMapper
import com.example.transfer.chart.domain.ChartConfigManager
import com.example.transfer.chart.domain.model.ChartConfig
import com.example.transfer.chart.domain.model.GraphSeries
import com.example.transfer.chart.domain.model.ParameterDisplayData
import com.example.transfer.protocol.domain.model.ByteData
import com.example.transfer.protocol.domain.model.Type
import com.example.transfer.protocol.domain.usecase.ObserveParametersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetVisibleChartDataUseCase @Inject constructor(
    private val chartConfigManager: ChartConfigManager,
    private val byteDataToGraphSeriesMapper: ByteDataToGraphSeriesMapper,
    private val observeParametersUseCase: ObserveParametersUseCase,
    private val parameterDisplayDataMapper: ParameterDisplayDataMapper

) {
    private val _parameterDisplayData = MutableStateFlow(ParameterDisplayData())
    val parameterDisplayData: StateFlow<ParameterDisplayData> = _parameterDisplayData.asStateFlow()

    val chartConfig: StateFlow<ChartConfig> = chartConfigManager.chartConfig

    private val cachedSeries = MutableStateFlow<List<GraphSeries>>(emptyList())
    private val pendingConfig = MutableStateFlow<ChartConfig?>(null)

    fun updateConfig(newConfig: ChartConfig) {
        if (cachedSeries.value.isNotEmpty()) {
            chartConfigManager.updateChartConfig(newConfig, cachedSeries.value)
        } else {
            pendingConfig.value = newConfig
        }
    }

    fun observeChartData(typeFlow: StateFlow<Type>): Flow<List<GraphSeries>> {
        val dataFlow = observeParametersUseCase.execute(typeFlow)
            .map(::extractRelevantData)
            .let(byteDataToGraphSeriesMapper::processData)

        return combine(
            dataFlow,
            chartConfig,
            byteDataToGraphSeriesMapper.selectedIndex,
            ::processChartData
        )
    }

    private fun processChartData(
        data: List<GraphSeries>,
        config: ChartConfig,
        index: Int?
    ): List<GraphSeries> {
        val resolvedConfig = pendingConfig.getAndUpdate { null } ?: config
        chartConfigManager.updateChartConfig(resolvedConfig, data)
        cachedSeries.value = data

        val visibleSeries = data.filterVisibleRange(
            startX = resolvedConfig.offset,
            count = resolvedConfig.stepCount
        )
        _parameterDisplayData.value = parameterDisplayDataMapper.map(visibleSeries, index)

        return visibleSeries
    }

    fun observeTime(): Flow<String> = byteDataToGraphSeriesMapper.timeFlow

    fun updateSelectedIndex(selectedIndex: Int?) {
        updateSelectedIndex(selectedIndex)
    }


    private fun extractRelevantData(rawData: List<ByteData>): List<ByteData> =
        rawData.take(VISIBLE_RANGE) + rawData.drop(HEADER_SIZE)

    private fun List<GraphSeries>.filterVisibleRange(
        startX: Float,
        count: Int
    ): List<GraphSeries> {
        val endX = startX + count
        return map { series ->
            series.copy(
                points = series.points
                    .filter { it.xCoordinate in startX..endX }
                    .sortedBy { it.xCoordinate }
            )
        }
    }

    companion object {
        private const val VISIBLE_RANGE = 128
        private const val HEADER_SIZE = 208
    }
}