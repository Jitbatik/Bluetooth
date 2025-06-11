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
class ChartConfigManager @Inject constructor() {

    private val _autoScrollEnabled = MutableStateFlow(true)
    private val _chartConfig = MutableStateFlow(ChartConfig())
    val chartConfig: StateFlow<ChartConfig> = _chartConfig.asStateFlow()

    fun updateChartConfig(newParams: ChartConfig, seriesList: List<GraphSeries>) {
        if (seriesList.isEmpty()) return

        val stepCount = calculateStepCount(newParams)
        val (minTime, maxTime) = findTimeBounds(seriesList)

        val maxOffsetX = (maxTime - minTime - stepCount.toFloat()).coerceAtLeast(0f)

        val currentConfig = _chartConfig.value
        val isManualScroll = hasUserScrolledManually(currentConfig, newParams)
        val isScaleChanged = hasScaleChanged(currentConfig, newParams)

        if (isManualScroll && newParams.offset < maxOffsetX - AUTO_SCROLL_ENABLE_OFFSET_THRESHOLD) {
            _autoScrollEnabled.value = false
        }

        val resolvedOffset = resolveOffset(currentConfig, newParams, maxOffsetX, isScaleChanged)

        _chartConfig.value = newParams.copy(
            maxOffsetX = maxOffsetX,
            offset = resolvedOffset
        )

        if (!isManualScroll && resolvedOffset >= maxOffsetX - AUTO_SCROLL_ENABLE_OFFSET_THRESHOLD) {
            _autoScrollEnabled.value = true
        }
    }

    private fun hasUserScrolledManually(current: ChartConfig, new: ChartConfig): Boolean =
        abs(new.offset - current.offset) > MANUAL_SCROLL_OFFSET_THRESHOLD

    private fun hasScaleChanged(current: ChartConfig, new: ChartConfig): Boolean =
        current.scale != new.scale

    private fun resolveOffset(
        current: ChartConfig,
        newParams: ChartConfig,
        maxOffsetX: Float,
        isScaleChanged: Boolean
    ): Float {
        return when {
            isScaleChanged -> {
                val relativePos = if (current.maxOffsetX > 0f) {
                    current.offset / current.maxOffsetX
                } else 0f
                (maxOffsetX * relativePos).coerceIn(0f, maxOffsetX)
            }
            _autoScrollEnabled.value -> maxOffsetX
            else -> newParams.offset.coerceIn(0f, maxOffsetX)
        }
    }

    private fun findTimeBounds(seriesList: List<GraphSeries>): Pair<Long, Long> {
        var minTs = Long.MAX_VALUE
        var maxTs = Long.MIN_VALUE
        for (s in seriesList) {
            for (p in s.points) {
                minTs = min(minTs, p.timestamp)
                maxTs = max(maxTs, p.timestamp)
            }
        }
        if (minTs == Long.MAX_VALUE) minTs = 0L
        if (maxTs == Long.MIN_VALUE) maxTs = 0L
        return minTs to maxTs
    }

    private fun calculateStepCount(param: ChartConfig): Int =
        (param.minScalePoint + ((param.scale - param.minScale) *
                (param.maxScalePoint - param.minScalePoint) / (param.maxScale - param.minScale))).toInt()

    companion object {
        private const val MANUAL_SCROLL_OFFSET_THRESHOLD = 0.1f
        private const val AUTO_SCROLL_ENABLE_OFFSET_THRESHOLD = 1f
    }
}
