package com.example.bluetooth.presentation.rssi

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.transfer.chart.domain.usecase.ObserveRSSIUseCase
import com.example.transfer.protocol.domain.model.Type
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import override.navigation.NavigationItem
import javax.inject.Inject


@HiltViewModel
class RssiViewModel @Inject constructor(
    observeRssiUseCase: ObserveRSSIUseCase,
    navigationStateHolder: NavigationStateHolder,
) : ViewModel() {

    private val observationType: StateFlow<Type> = navigationStateHolder.currentScreen
        .map { screen ->
            when (screen) {
                NavigationItem.Home -> Type.READ
                NavigationItem.ParametersDashboard -> Type.READ
                else -> Type.NOTHING
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Type.NOTHING
        )

    private val _rssi = observeRssiUseCase(observationType).toUIRssi()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RSSI("", Color.Gray)
        )
    val rssi: StateFlow<RSSI> = _rssi


}

