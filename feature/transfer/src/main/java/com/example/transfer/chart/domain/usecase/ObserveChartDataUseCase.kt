package com.example.transfer.chart.domain.usecase

import com.example.transfer.chart.data.ChartSettingsRepository
import com.example.transfer.chart.domain.SignalUtils
import com.example.transfer.chart.domain.model.DataPoint
import com.example.transfer.chart.domain.model.GraphSeries
import com.example.transfer.chart.domain.model.SignalSettings
import com.example.transfer.protocol.data.LiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ObserveChartDataUseCase @Inject constructor(
    private val liftRepository: LiftRepository,
    private val settingsRepository: ChartSettingsRepository,
) {
    private val state = MutableStateFlow(State())

    operator fun invoke(): Flow<List<GraphSeries>> {
        return combine(
            liftRepository.observeLiftData(),
            settingsRepository.observe()
        ) { byteData, settings ->
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
        byteData: List<Byte>,
        signals: List<SignalSettings>,
        timestampSignal: SignalSettings?,
        millisSignal: SignalSettings?,
        baseTimeMs: Long?,
    ): State {
        if (byteData.size < MIN_HEADER_SIZE || timestampSignal == null || millisSignal == null) {
            return State(existingSeries, baseTimeMs)
        }

        val timestamp = SignalUtils
            .extractSignalValueFromByteData(byteData, timestampSignal.offset, timestampSignal.type)
            .toLong()
        val millis = SignalUtils
            .extractSignalValueFromByteData(byteData, millisSignal.offset, millisSignal.type)


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
                    SignalUtils.extractSignalValueFromByteData(byteData, signal.offset, signal.type)
                        .toFloat()
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