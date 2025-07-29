package com.example.transfer.chart.domain.usecase

import com.example.transfer.chart.data.ChartSettingsRepository
import com.example.transfer.chart.domain.SignalUtils
import com.example.transfer.protocol.domain.model.Type
import com.example.transfer.protocol.domain.usecase.ObserveParametersUseCase
import com.example.transfer.protocol.domain.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveTimeUseCase @Inject constructor(
    private val observeParametersUseCase: ObserveParametersUseCase,
    private val settingsRepository: ChartSettingsRepository,
) {
    operator fun invoke(typeFlow: StateFlow<Type>): Flow<String> {
        val chartSettingsFlow = settingsRepository.observe()
        val byteDataFlow = observeParametersUseCase.execute(typeFlow)

        return combine(chartSettingsFlow, byteDataFlow) { settings, byteDataList ->
            val timestampSignal = settings.config.timestampSignal
            val millisSignal = settings.config.millisSignal

            if (timestampSignal == null || millisSignal == null) return@combine ""

            val timestamp = SignalUtils.extractSignalValue(
                byteDataList,
                timestampSignal.offset,
                timestampSignal.type
            ).toLong()
            val millis = SignalUtils.extractSignalValue(byteDataList, millisSignal.offset, millisSignal.type)

            DateTimeUtils.formatTimeWithMillis(timestamp, millis)
        }
    }

}