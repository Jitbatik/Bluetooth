package com.psis.transfer.chart.domain.usecase

import android.util.Log
import com.psis.transfer.chart.domain.ChartConfigRepository
import com.psis.transfer.chart.domain.model.ChartConfig
import com.psis.transfer.chart.domain.model.GraphSeries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class UpdateChartScaleUseCase @Inject constructor(
    private val repository: ChartConfigRepository
) {
    operator fun invoke(scale: Float) {
        val current = repository.observe().value
        repository.update(current.copy(scale = scale))
    }
}

class UpdateChartOffsetUseCase @Inject constructor(
    private val repository: ChartConfigRepository
) {
    operator fun invoke(offset: Float) {
        val current = repository.observe().value
        val clampedOffset = offset.coerceIn(current.minOffsetX, current.maxOffsetX)
        Log.d("UpdateChartOffsetUseCase", "minOffsetX = ${current.minOffsetX}")
        Log.d("UpdateChartOffsetUseCase", "maxOffsetX = ${current.maxOffsetX}")
        repository.update(current.copy(offset = clampedOffset))
    }
}

//TODO нужно чтобы офсет был от отричательного до положительного или 0
class ChartRangeObserver @Inject constructor(
    private val chartConfigRepository: ChartConfigRepository,
) {
    fun start(
        scope: CoroutineScope,
        chartDataFlow: Flow<List<ChartFrame>>,
        chartConfigFlow: StateFlow<ChartConfig>
    ) {
        scope.launch {
            chartDataFlow
                .map { frames ->
                    val timestamps = frames.map { it.timestampSeconds }

                    val minTs = timestamps.minOrNull() ?: 0
                    val maxTs = timestamps.maxOrNull() ?: 0
                    minTs to maxTs
                }
                .distinctUntilChanged()
                .collect { (minTime, maxTime) ->
                    val config = chartConfigFlow.value

                    val updatedConfig = config.copy(
                        minOffsetX = (minTime - maxTime).toFloat(),
                        maxOffsetX = 0f
                    )

                    if (updatedConfig != config) {
                        chartConfigRepository.update(updatedConfig)
                    }
                }
        }
    }
}
