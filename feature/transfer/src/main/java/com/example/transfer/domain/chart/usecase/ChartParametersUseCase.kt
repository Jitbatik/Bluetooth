package com.example.transfer.domain.chart.usecase

import com.example.transfer.domain.chart.processor.ParametersProcessor
import com.example.transfer.domain.chart.config.ChartConfigManager
import com.example.transfer.domain.parameters.Type
import com.example.transfer.domain.parameters.usecase.ObserveParametersUseCase
import com.example.transfer.model.ByteData
import com.example.transfer.model.ChartConfig
import com.example.transfer.model.LiftParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ChartParametersUseCase @Inject constructor(
    private val chartConfigManager: ChartConfigManager,
    private val parametersProcessor: ParametersProcessor,
    private val observeParametersUseCase: ObserveParametersUseCase
) {
    val chartConfig: StateFlow<ChartConfig> = chartConfigManager.chartConfig

    fun updateConfig(newConfig: ChartConfig) {
        chartConfigManager.updateChartParameter(
            newConfig,
            parametersProcessor.getAccumulatedParameters()
        )
        parametersProcessor.refreshFilter(chartConfig.value)
    }

    fun observeChartData(typeFlow: StateFlow<Type>): Flow<List<LiftParameters>> =
        observeParametersUseCase.execute(typeFlow)
            .map { rawData -> extractRelevantData(rawData) }
            .let { dataFlow -> parametersProcessor.processParameters(dataFlow, chartConfig) }
            .onEach { updateConfig(chartConfig.value) }

    private fun extractRelevantData(rawData: List<ByteData>): List<ByteData> =
        rawData.take(128) + rawData.drop(208)
}