package com.example.transfer.chart.domain.usecase


import com.example.transfer.chart.domain.SignalUtils
import com.example.transfer.protocol.data.LiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveRSSIUseCase @Inject constructor(
    private val liftRepository: LiftRepository,
) {
    operator fun invoke(): Flow<Int> = liftRepository
        .observeLiftData()
        .map { byteData ->
            if (byteData.size < 102) {
                -200
            } else {
                val raw = SignalUtils.extractSignalValueFromByteData(byteData, 101, "u8")
                if (raw > 127) raw - 256 else raw
            }
        }
}
