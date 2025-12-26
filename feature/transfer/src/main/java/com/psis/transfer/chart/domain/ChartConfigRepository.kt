package com.psis.transfer.chart.domain

import com.psis.transfer.chart.domain.model.ChartConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChartConfigRepository @Inject constructor() {
    private val _chartConfig = MutableStateFlow(ChartConfig())

    fun observe() = _chartConfig.asStateFlow()

    fun update(config: ChartConfig) {
        _chartConfig.value = config
    }

    fun update(transform: (ChartConfig) -> ChartConfig) {
        _chartConfig.update(transform)
    }
}