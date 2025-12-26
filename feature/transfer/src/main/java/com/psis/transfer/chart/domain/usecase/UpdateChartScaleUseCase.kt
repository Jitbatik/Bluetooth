package com.psis.transfer.chart.domain.usecase

import com.psis.transfer.chart.domain.ChartConfigRepository
import javax.inject.Inject

class UpdateChartScaleUseCase @Inject constructor(
    private val repository: ChartConfigRepository
) {
    operator fun invoke(scale: Float) {
        val current = repository.observe().value
        repository.update(current.copy(scale = scale))
    }
}