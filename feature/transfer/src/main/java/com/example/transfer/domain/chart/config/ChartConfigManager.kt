package com.example.transfer.domain.chart.config

import com.example.transfer.model.ChartConfig
import com.example.transfer.model.LiftParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.abs

class ChartConfigManager @Inject constructor() {
    private val _autoScrollEnabled = MutableStateFlow(true)
    private val _chartConfig = MutableStateFlow(ChartConfig())
    val chartConfig: StateFlow<ChartConfig> = _chartConfig.asStateFlow()

    fun updateChartParameter(newParams: ChartConfig, parameters: List<LiftParameters>) {
        if (parameters.isEmpty()) {
            _chartConfig.value = newParams.copy(maxOffsetX = 0f)
            return
        }

        val stepCount = calculateStepCount(newParams)
        val minTime = parameters.minOf { it.timestamp }
        val maxTime = parameters.maxOf { it.timestamp }

        val visibleWindow = stepCount * 1f
        val maxOffsetX = (maxTime - minTime - visibleWindow).coerceAtLeast(0f)

        val isManualScroll = abs(newParams.offset - _chartConfig.value.offset) > 0.1f

        if (isManualScroll && newParams.offset < maxOffsetX - 1f) {
            _autoScrollEnabled.value = false
        }

        val isScaleChanged = newParams.scale != _chartConfig.value.scale

        val newOffset = when {
            isScaleChanged -> {
                val relativePosition = if (_chartConfig.value.maxOffsetX > 0)
                    _chartConfig.value.offset / _chartConfig.value.maxOffsetX
                else 0f
                (maxOffsetX * relativePosition).coerceIn(0f, maxOffsetX)
            }
            _autoScrollEnabled.value -> maxOffsetX
            else -> newParams.offset.coerceIn(0f, maxOffsetX)
        }

        _chartConfig.update {
            newParams.copy(
                stepCount = stepCount,
                maxOffsetX = maxOffsetX,
                offset = newOffset
            )
        }

        if (newOffset >= maxOffsetX - 1f && !isManualScroll) {
            _autoScrollEnabled.value = true
        }
    }

    private fun calculateStepCount(param: ChartConfig): Int =
        (param.minScalePoint + ((param.scale - param.minScale) *
                (param.maxScalePoint - param.minScalePoint) / (param.maxScale - param.minScale))).toInt()
} 