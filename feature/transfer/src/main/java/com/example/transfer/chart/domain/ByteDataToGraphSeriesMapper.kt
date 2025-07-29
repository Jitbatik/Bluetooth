package com.example.transfer.chart.domain

import com.example.transfer.chart.data.ChartSettingsRepository
import com.example.transfer.chart.domain.model.DataPoint
import com.example.transfer.chart.domain.model.GraphSeries
import com.example.transfer.chart.domain.model.SignalSettings
import com.example.transfer.protocol.domain.model.ByteData
import com.example.transfer.protocol.domain.model.Type
import com.example.transfer.protocol.domain.usecase.ObserveParametersUseCase
import com.example.transfer.protocol.domain.utils.ByteUtils.getBitLE
import com.example.transfer.protocol.domain.utils.ByteUtils.toIntLE
import com.example.transfer.protocol.domain.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.scan
import javax.inject.Inject


class TransformSeriesUseCase @Inject constructor(
    private val protocolRepo: ObserveParametersUseCase,
    private val settingsRepo: ChartSettingsRepository,
    private val mapper: ByteDataToGraphSeriesMapper1
) {
    fun execute(
        typeFlow: StateFlow<Type>
    ): Flow<List<GraphSeries>> = combine(
        protocolRepo.execute(typeFlow),     // Flow<List<ByteData>>
        settingsRepo.observe()              // StateFlow<ChartSettings>
    ) { byteDataList, settings ->
        byteDataList to settings.config
    }.scan(emptyList()) { accumulated, (byteDataList, config) ->
        mapper.process(
            existingSeries = accumulated,
            byteData = byteDataList,
            settings = config.signals,
            timestampSignal = config.timestampSignal,
            millisSignal = config.millisSignal
        )
    }
}


class ByteDataToGraphSeriesMapper1 {
    private var firstTimestamp: Long? = null

    private val _timeFlow = MutableStateFlow("")

    fun process(
        existingSeries: List<GraphSeries>,
        byteData: List<ByteData>,
        settings: List<SignalSettings>,
        timestampSignal: SignalSettings?,
        millisSignal: SignalSettings?
    ): List<GraphSeries> {
        if (byteData.size < MIN_HEADER_SIZE) return existingSeries
        if (timestampSignal == null || millisSignal == null) return existingSeries
        val timestamp =
            extractSignalValue(byteData, timestampSignal.offset, timestampSignal.type).toLong()
        val millis = extractSignalValue(byteData, millisSignal.offset, millisSignal.type)


        updateInitialTimestampIfNeeded(timestamp)
        updateTimeFlow(timestamp, millis)

        return mergeSignals(existingSeries, byteData, settings, timestamp, millis)
    }

    private fun extractSignalValue(byteData: List<ByteData>, offset: Int, type: String): Int {
        return when {
            type.matches(Regex("b[0-7]")) -> {
                byteData.subList(offset, getSignalSize(offset, type)).getBitLE(
                    type.removePrefix("b").toInt()
                )
            }

            else -> {
                byteData.subList(offset, getSignalSize(offset, type)).toIntLE()
            }
        }
    }

    private fun getSignalSize(offset: Int, type: String): Int {
        return when {
            type == "u32" -> offset + 4
            type == "u16" -> offset + 2
            type == "u8" || type == "e8" -> offset + 1
//            type == "m40" -> offset + 5
//            type == "m64" -> offset + 8
            type.matches(Regex("b[0-7]")) -> offset + 1

            else -> offset + 1

        }
    }

    private fun updateInitialTimestampIfNeeded(timestamp: Long) {
        if (firstTimestamp == null) firstTimestamp = timestamp
    }

    private fun updateTimeFlow(timestamp: Long, millis: Int) {
        _timeFlow.value = DateTimeUtils.formatTimeWithMillis(timestamp, millis)
    }

    private fun mergeSignals(
        existingSeries: List<GraphSeries>,
        byteData: List<ByteData>,
        settings: List<SignalSettings>,
        timestamp: Long,
        millis: Int
    ): List<GraphSeries> {
        val xCoordinate = (timestamp - (firstTimestamp ?: timestamp)).toFloat()
        val updatedSeriesMap = existingSeries.associateBy { it.name }.toMutableMap()

        settings.forEach { signal ->
            if (byteData.size <= getSignalSize(signal.offset, signal.type)) return@forEach

            val yValue = extractSignalValue(byteData, signal.offset, signal.type).toFloat()
            val point = DataPoint(xCoordinate, yValue, timestamp, millis)

            val existingPoints = updatedSeriesMap[signal.name]?.points.orEmpty()
            updatedSeriesMap[signal.name] = GraphSeries(
                name = signal.name,
                points = existingPoints + point,
                color = signal.color
            )
        }

        return updatedSeriesMap.values.toList()
    }

    companion object {
        private const val MIN_HEADER_SIZE = 6
    }
}

class ByteDataToGraphSeriesMapper @Inject constructor(
    private val parameterSettings: ChartSettingsRepository
) {
    private var firstTimestamp: Long? = null

    private val _timeFlow = MutableStateFlow("")
    val timeFlow: StateFlow<String> get() = _timeFlow.asStateFlow()

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    val selectedIndex = _selectedIndex.asStateFlow()
    fun updateSelectedIndex(selectedIndex: Int?) {
        this._selectedIndex.value = selectedIndex
    }

    fun processData(dataFlow: Flow<List<ByteData>>): Flow<List<GraphSeries>> {
        return combine(
            dataFlow,
            parameterSettings.observe()
        ) { byteDataList, settings ->
            val visibleSignals = settings.config.signals.filter { it.isVisible }

            val timestampSignal = settings.config.timestampSignal
            val millisSignal = settings.config.millisSignal

            Triple(byteDataList, visibleSignals, timestampSignal to millisSignal)
        }.scan(emptyList()) { accumulated, (byteDataList, visibleSignals, timePair) ->
            val (timestampSignal, millisSignal) = timePair

            val visibleSignalIds = visibleSignals.map { it.name }.toSet()
            val filteredAccumulated = accumulated.filter { it.name in visibleSignalIds }
            processParameters(
                existingSeries = filteredAccumulated,
                byteData = byteDataList,
                signals = visibleSignals,
                timestampSignal = timestampSignal,
                millisSignal = millisSignal
            )
        }
    }


    private fun processParameters(
        existingSeries: List<GraphSeries>,
        byteData: List<ByteData>,
        signals: List<SignalSettings>,
        timestampSignal: SignalSettings?,
        millisSignal: SignalSettings?
    ): List<GraphSeries> {
        if (byteData.size < MIN_HEADER_SIZE) return existingSeries

        if (timestampSignal == null || millisSignal == null) return existingSeries

        val timestamp =
            extractSignalValue(byteData, timestampSignal.offset, timestampSignal.type).toLong()
        val millis = extractSignalValue(byteData, millisSignal.offset, millisSignal.type)

        updateInitialTimestampIfNeeded(timestamp)
        updateTimeFlow(timestamp, millis)

        val displaySignals = signals.filter { it.isVisible && it.name != "Time" && it.name != "ms" }

        return mergeSignals(existingSeries, byteData, displaySignals, timestamp, millis)
    }

    private fun getSignalSize(offset: Int, type: String): Int {
        return when {
            type == "u32" -> offset + 4
            type == "u16" -> offset + 2
            type == "u8" || type == "e8" -> offset + 1
//            type == "m40" -> offset + 5
//            type == "m64" -> offset + 8
            type.matches(Regex("b[0-7]")) -> offset + 1

            else -> offset + 1

        }
    }

    private fun extractSignalValue(byteData: List<ByteData>, offset: Int, type: String): Int {
        return when {
            type.matches(Regex("b[0-7]")) -> {
                byteData.subList(offset, getSignalSize(offset, type)).getBitLE(
                    type.removePrefix("b").toInt()
                )
            }

            else -> {
                byteData.subList(offset, getSignalSize(offset, type)).toIntLE()
            }
        }
    }


    private fun updateInitialTimestampIfNeeded(timestamp: Long) {
        if (firstTimestamp == null) firstTimestamp = timestamp
    }

    private fun updateTimeFlow(timestamp: Long, millis: Int) {
        _timeFlow.value = DateTimeUtils.formatTimeWithMillis(timestamp, millis)
    }

    private fun mergeSignals(
        existingSeries: List<GraphSeries>,
        byteData: List<ByteData>,
        signals: List<SignalSettings>,
        timestamp: Long,
        millis: Int
    ): List<GraphSeries> {
        val xCoordinate = (timestamp - (firstTimestamp ?: timestamp)).toFloat()
        val updatedSeriesMap = existingSeries.associateBy { it.name }.toMutableMap()

        signals.forEach { signal ->
            if (byteData.size <= getSignalSize(signal.offset, signal.type)) return@forEach

            val yValue = extractSignalValue(byteData, signal.offset, signal.type).toFloat()
            val point = DataPoint(xCoordinate, yValue, timestamp, millis)

            val existingPoints = updatedSeriesMap[signal.name]?.points.orEmpty()
            updatedSeriesMap[signal.name] = GraphSeries(
                name = signal.name,
                points = existingPoints + point,
                color = signal.color
            )
        }

        return updatedSeriesMap.values.toList()
    }

    companion object {
        private const val MIN_HEADER_SIZE = 6
    }
}