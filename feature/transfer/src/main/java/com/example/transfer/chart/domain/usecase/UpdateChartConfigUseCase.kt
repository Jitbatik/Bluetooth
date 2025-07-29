package com.example.transfer.chart.domain.usecase

import android.util.Log
import com.example.transfer.chart.domain.ChartConfigRepository
import com.example.transfer.chart.domain.model.ChartConfig
import com.example.transfer.chart.domain.model.GraphSeries
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

class ChartRangeObserver @Inject constructor(
    private val chartConfigRepository: ChartConfigRepository,
) {

    fun start(
        scope: CoroutineScope,
        chartDataFlow: Flow<List<GraphSeries>>,
        chartConfigFlow: StateFlow<ChartConfig>
    ) {
        scope.launch {
            chartDataFlow
                .map { seriesList ->
                    var minTs = Float.MAX_VALUE
                    var maxTs = Float.MIN_VALUE

                    for (series in seriesList) {
                        for (point in series.points) {
                            minTs = min(minTs, point.xCoordinate)
                            maxTs = max(maxTs, point.xCoordinate)
                        }
                    }
                    Log.d("ChartRangeObserver", "$seriesList")


                    Pair(
                        if (minTs == Float.MAX_VALUE) 0f else minTs,
                        if (maxTs == Float.MIN_VALUE) 0f else maxTs
                    )
                }
                .distinctUntilChanged()
                .collect { (minTime, maxTime) ->
                    val config = chartConfigFlow.value
                    val stepCount = config.stepCount.toFloat()
                    val newMaxOffset = (maxTime - minTime - stepCount).coerceAtLeast(0f)

                    val updatedConfig = config.copy(
                        minOffsetX = minTime,
                        maxOffsetX = newMaxOffset
                    )

                    if (updatedConfig != config) {
                        chartConfigRepository.update(updatedConfig)
                    }
                }
        }
    }
}
