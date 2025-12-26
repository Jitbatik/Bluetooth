package com.psis.transfer.chart.domain.usecase

import android.util.Log
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.chart.domain.VersionSignalInfoProvider
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class ObserveElevatorVersionUseCase @Inject constructor(
    private val elevatorStateRepository: ElevatorStateRepository,
    private val versionSignalInfoProvider: VersionSignalInfoProvider
) {
    operator fun invoke(): Flow<Int> {
        val (offset, type) = versionSignalInfoProvider.getVersionSignalInfo()

        return elevatorStateRepository.observeElevatorState()
            .map { bytes -> bytes.slice(3..bytes.size - 2) }
            .mapNotNull { byteData ->
                if (byteData.size <= 80) {
                    Log.w("ElevatorVersion", "Skip packet: size=${byteData.size}, too short")
                    return@mapNotNull null
                }
                SignalUtils.extractSignalValueFromByteData(byteData, offset, type)
            }
            .distinctUntilChanged()
    }
}