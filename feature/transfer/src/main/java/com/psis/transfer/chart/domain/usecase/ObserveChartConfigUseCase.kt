package com.psis.transfer.chart.domain.usecase

import com.psis.transfer.chart.domain.ChartConfigRepository
import com.psis.transfer.chart.domain.model.ChartConfig
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveChartConfigUseCase @Inject constructor(
    private val chartConfigRepository: ChartConfigRepository,
) {
    operator fun invoke(): StateFlow<ChartConfig> {
        return chartConfigRepository.observe()
    }
}