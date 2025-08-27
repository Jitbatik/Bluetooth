package com.example.transfer.chart.domain.usecase

import com.example.transfer.chart.data.ChartSettingsRepository
import com.example.transfer.chart.domain.SignalUtils
import com.example.transfer.chart.domain.model.ChartSettings
import com.example.transfer.chart.domain.model.DataPoint
import com.example.transfer.chart.domain.model.GraphSeries
import com.example.transfer.chart.domain.model.SignalSettings
import com.example.transfer.protocol.domain.model.ByteData
import com.example.transfer.protocol.domain.model.Type
import com.example.transfer.protocol.domain.usecase.ObserveParametersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ObserveChartDataUseCase @Inject constructor(
    private val observeParametersUseCase: ObserveParametersUseCase,
    private val settingsRepository: ChartSettingsRepository,
) {
    operator fun invoke(
        observationType: StateFlow<Type>,
        scope: CoroutineScope
    ): Flow<List<GraphSeries>> {
        val byteDataFlow = observeParametersUseCase.execute(observationType)

        scope.launch {
            initializeChartSettingsFromVersion(byteDataFlow)
        }


        return observeChartData(byteDataFlow)
    }

    private fun observeChartData(byteDataFlow: Flow<List<ByteData>>): Flow<List<GraphSeries>> {
        return processChartData(
            byteDataFlow = byteDataFlow,
            chartSettingsFlow = settingsRepository.observe()
        )
    }

    private suspend fun initializeChartSettingsFromVersion(byteDataFlow: Flow<List<ByteData>>) {
        val (offset, type) = settingsRepository.getVersionSignalInfo()

        byteDataFlow
            .mapNotNull { byteData ->
                SignalUtils.extractSignalValue(byteData, offset, type)
            }
            .distinctUntilChanged()
            .collect { version ->
                settingsRepository.initIfNeeded(version)
            }
    }

    private val state = MutableStateFlow(State())
    private fun processChartData(
        byteDataFlow: Flow<List<ByteData>>,
        chartSettingsFlow: StateFlow<ChartSettings>
    ): Flow<List<GraphSeries>> {
        return combine(byteDataFlow, chartSettingsFlow) { byteData, settings ->
            byteData to settings.config
        }.onEach { (byteData, config) ->
            val newState = processParameters(
                existingSeries = state.value.graphSeries,
                byteData = byteData,
                signals = config.signals,
                timestampSignal = config.timestampSignal,
                millisSignal = config.millisSignal,
                baseTimeMs = state.value.baseTimeMs
            )
            state.value = newState
        }.map { state.value.graphSeries }
    }


    private fun processParameters(
        existingSeries: List<GraphSeries>,
        byteData: List<ByteData>,
        signals: List<SignalSettings>,
        timestampSignal: SignalSettings?,
        millisSignal: SignalSettings?,
        baseTimeMs: Long?,
    ): State {
        if (byteData.size < MIN_HEADER_SIZE || timestampSignal == null || millisSignal == null) {
            return State(existingSeries, baseTimeMs)
        }

        val timestamp = SignalUtils
            .extractSignalValue(byteData, timestampSignal.offset, timestampSignal.type)
            .toLong()
        val millis = SignalUtils
            .extractSignalValue(byteData, millisSignal.offset, millisSignal.type)


        // Полное время текущей точки в мс
        val currentTimeMs = timestamp * 1000L + millis

        // База: первая увиденная полная метка времени в мс
        val base = baseTimeMs ?: currentTimeMs

        // x в секундах с долями (миллисекунды учтены)
        val x = (currentTimeMs - base).toFloat() / 1000f

        val visibleSignals = signals.filter { it.name !in listOf("Time", "ms") && it.isVisible }
        val updatedSeries = existingSeries.associateBy { it.name }.toMutableMap()

        visibleSignals.forEach { signal ->
            if (byteData.size > SignalUtils.getSignalSize(signal.offset, signal.type)) {
                val y =
                    SignalUtils.extractSignalValue(byteData, signal.offset, signal.type).toFloat()
                val point = DataPoint(x, y, timestamp, millis)

                val series = updatedSeries.getOrPut(signal.name) {
                    GraphSeries(signal.name, mutableListOf(), signal.color)
                }
                series.addPoint(point)

            }
        }

        return State(updatedSeries.values.toList(), base)
    }


    private data class State(
        val graphSeries: List<GraphSeries> = emptyList(),
        val baseTimeMs: Long? = null,
    )


    companion object {
        private const val MIN_HEADER_SIZE = 6
    }

}

class ObserveChartSettings @Inject constructor(
    private val repository: ChartSettingsRepository,
) {
    operator fun invoke(): Flow<ChartSettings> {
        return repository.observe()
    }
}


fun List<GraphSeries>.filterVisibleRange(
    startX: Float,
    count: Int,
): List<GraphSeries> {
    val endX = startX + count

    // Шаг 1: фильтрация по диапазону
    val filtered = map { series ->
        val filteredPoints = series.points
            .asSequence()
            .filter { it.xCoordinate in startX..endX }
            .sortedBy { it.xCoordinate }
            .toMutableList()

        series.copyWithPoints(filteredPoints)
    }

    // Шаг 2: находим минимальное значение x среди всех точек
    val allFilteredPoints = filtered.flatMap { it.points }
    val minX = allFilteredPoints.minByOrNull { it.xCoordinate }?.xCoordinate ?: 0f

    // Шаг 3: нормализуем все x на основе minX
    val normalized = filtered.mapIndexed { _, series ->
        val newPoints = series.points.map { point ->
            point.copy(xCoordinate = point.xCoordinate - minX)
        }.toMutableList()
        series.copyWithPoints(newPoints)
    }

    return normalized
}

