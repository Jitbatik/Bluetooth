package com.psis.elimlift.presentation.rssi

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psis.transfer.chart.domain.usecase.ObserveRSSIUseCase
import com.psis.transfer.protocol.domain.SessionManagerState
import com.psis.transfer.protocol.domain.usecase.LiftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class RssiViewModel @Inject constructor(
    observeRssiUseCase: ObserveRSSIUseCase,
    liftUseCase: LiftUseCase,
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    private val _rssi: StateFlow<RSSI> = liftUseCase
        .observeLiftSession()
        .onEach { Log.d("RssiViewModel", it.toString()) }
        .map { sessionState ->
            if (sessionState.hasState(SessionManagerState.State.STARTED)) {
                observeRssiUseCase().toUIRssi() // читаем реальные данные
            } else {
                flowOf(RSSI("нет сигнала", Color.Gray)) // когда сессия не активна
            }
        }
        .flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RSSI("нет сигнала", Color.Gray)
        )
    val rssi: StateFlow<RSSI> = _rssi
}