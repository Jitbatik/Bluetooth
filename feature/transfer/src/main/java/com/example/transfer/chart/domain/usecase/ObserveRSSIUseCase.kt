package com.example.transfer.chart.domain.usecase

import com.example.transfer.chart.domain.SignalUtils
import com.example.transfer.protocol.domain.model.Type
import com.example.transfer.protocol.domain.usecase.ObserveParametersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class ObserveRSSIUseCase @Inject constructor(
    private val observeParametersUseCase: ObserveParametersUseCase,
) {
    operator fun invoke(
        observationType: StateFlow<Type>,
    ): Flow<Int> {
        val byteDataFlow = observeParametersUseCase.execute(observationType)

        return byteDataFlow.mapNotNull { byteData ->
            val raw = SignalUtils.extractSignalValue(byteData, 101, "u8")
            raw.let { if (it > 127) it - 256 else it }
        }
    }
}