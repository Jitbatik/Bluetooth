package com.psis.transfer.chart.domain.usecase

import com.psis.transfer.chart.data.ChartSettingsRepository
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.protocol.data.LiftRepository
import com.psis.transfer.protocol.domain.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveTimeUseCase @Inject constructor(
    private val liftRepository: LiftRepository,
    private val settingsRepository: ChartSettingsRepository,
) {
    operator fun invoke(): Flow<String> {
        val chartSettingsFlow = settingsRepository.observe()
        val byteDataFlow = liftRepository.observeLiftData()

        return combine(chartSettingsFlow, byteDataFlow) { settings, byteDataList ->
            if (byteDataList.isEmpty()) {
                return@combine ""
            }
            val timestampSignal = settings.config.timestampSignal
            val millisSignal = settings.config.millisSignal

            if (timestampSignal == null || millisSignal == null) return@combine ""

            val timestamp = SignalUtils.extractSignalValueFromByteData(
                byteDataList,
                timestampSignal.offset,
                timestampSignal.type
            ).toLong()
            val millis = SignalUtils.extractSignalValueFromByteData(
                byteDataList,
                millisSignal.offset,
                millisSignal.type
            )

            DateTimeUtils.formatTimeWithMillis(timestamp, millis)
        }
    }

}