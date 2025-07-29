package com.example.transfer.chart.domain

import com.example.transfer.chart.domain.model.ChartConfig
import com.example.transfer.chart.domain.model.GraphSeries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@Singleton
class ChartConfigRepository @Inject constructor() {
    private val _chartConfig = MutableStateFlow(ChartConfig())

    fun observe() = _chartConfig.asStateFlow()

    fun update(config: ChartConfig) {
        _chartConfig.value = config
    }

}