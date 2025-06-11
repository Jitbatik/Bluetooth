package com.example.transfer.chart.domain

import com.example.transfer.chart.data.ChartSettingsRepository
import com.example.transfer.chart.domain.model.DataPoint
import com.example.transfer.chart.domain.model.GraphSeries
import com.example.transfer.chart.domain.model.SignalSettings
import com.example.transfer.protocol.domain.model.ByteData
import com.example.transfer.protocol.domain.utils.ByteUtils.toIntFromByteData
import com.example.transfer.protocol.domain.utils.ByteUtils.toLongFromByteData
import com.example.transfer.protocol.domain.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class ByteDataToGraphSeriesMapper @Inject constructor(
    private val parameterSettings: ChartSettingsRepository
) {

    private var firstTimestamp: Long? = null
    private val _timeFlow = MutableStateFlow("")
    val timeFlow: StateFlow<String> get() = _timeFlow.asStateFlow()

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    val selectedIndex = _selectedIndex.asStateFlow()
    fun updateSelectedIndex(selectedIndex: Int?) { this.selectedIndex.value = selectedIndex }

    fun processData(dataFlow: Flow<List<ByteData>>): Flow<List<GraphSeries>> {
        return combine(
            dataFlow,
            parameterSettings.chartSettings
        ) { byteDataList, settings ->
            byteDataList to settings.signals.filter { it.isVisible }
        }.scan(emptyList()) { accumulated, (byteDataList, signals) ->
            processParameters(accumulated, byteDataList, signals)
        }
    }


    private fun processParameters(
        existingSeries: List<GraphSeries>,
        byteData: List<ByteData>,
        signals: List<SignalSettings>
    ): List<GraphSeries> {
        if (byteData.size < MIN_HEADER_SIZE) return existingSeries

        val timestamp = byteData.subList(0, 4).toLongFromByteData()
        val millis = byteData.subList(4, 6).toIntFromByteData()

        updateInitialTimestampIfNeeded(timestamp)
        updateTimeFlow(timestamp, millis)
        return mergeSignals(existingSeries, byteData, signals, timestamp, millis)

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
            if (byteData.size <= signal.end) return@forEach

            val yValue = byteData.subList(signal.start, signal.end).toIntFromByteData().toFloat()
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