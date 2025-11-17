package com.psis.transfer.chart.domain.usecase


import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveRSSIUseCase @Inject constructor(
    private val stateRepository: ElevatorStateRepository,
) {
    operator fun invoke(): Flow<Int> = stateRepository
        .observeElevatorState()
        .map { byteData ->
            if (byteData.size < 102) {
                -200
            } else {
                val raw = SignalUtils.extractSignalValueFromByteData(byteData, 101, "u8")
                if (raw > 127) raw - 256 else raw
            }
        }
}
