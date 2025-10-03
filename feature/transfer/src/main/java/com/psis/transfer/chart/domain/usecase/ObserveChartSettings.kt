package com.psis.transfer.chart.domain.usecase

import android.util.Log
import com.psis.transfer.chart.data.ChartSettingsRepository
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.chart.domain.model.ChartSettings
import com.psis.transfer.protocol.data.LiftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class ObserveChartSettings @Inject constructor(
    private val repository: ChartSettingsRepository,
    private val liftRepository: LiftRepository,
) {
    operator fun invoke(scope: CoroutineScope): Flow<ChartSettings> {
        val byteDataFlow = liftRepository.observeLiftData()

        scope.launch {
            initializeChartSettingsFromVersion(byteDataFlow)
        }

        return repository.observe()
    }

    private suspend fun initializeChartSettingsFromVersion(byteDataFlow: Flow<List<Byte>>) {
        val (offset, type) = repository.getVersionSignalInfo()

        byteDataFlow
            .mapNotNull { byteData ->
                if (byteData.size <= 80) {
                    Log.w("ChartSettings", "Skip packet: size=${byteData.size}, too short")
                    return@mapNotNull null
                }
                SignalUtils.extractSignalValueFromByteData(byteData, offset, type)
            }
            .distinctUntilChanged()
            .collect { version ->
                try {
                    repository.initIfNeeded(version)
                    Log.d("ChartSettings", "Initialized with version=$version")
                } catch (e: IllegalStateException) {
                    Log.e("ChartSettings", "Unknown version=$version", e)
                }
            }
    }
}