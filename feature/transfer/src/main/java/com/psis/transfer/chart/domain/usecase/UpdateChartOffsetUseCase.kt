package com.psis.transfer.chart.domain.usecase

import com.psis.transfer.chart.domain.ChartConfigRepository
import javax.inject.Inject


class UpdateChartOffsetUseCase @Inject constructor(
    private val repository: ChartConfigRepository
) {
    operator fun invoke(offset: Float) {
        val current = repository.observe().value
        val clampedOffset = offset
        repository.update(current.copy(offset = clampedOffset))
    }
}