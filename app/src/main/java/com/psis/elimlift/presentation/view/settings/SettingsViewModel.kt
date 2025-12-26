package com.psis.elimlift.presentation.view.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psis.elimlift.domain.SettingsManager
import com.psis.elimlift.presentation.view.settings.model.BluetoothEvent
import com.psis.elimlift.presentation.view.settings.model.SettingsEvent
import com.psis.elimlift.presentation.view.settings.model.SettingsState
import com.psis.elimlift.presentation.view.settings.model.SignalEvent
import com.psis.elimlift.presentation.view.settings.model.WirelessBluetoothMask
import com.psis.elimlift.presentation.view.settings.utils.userChartSettingsMapToUI
import com.psis.transfer.chart.data.SignalUserSettingsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsManager: SettingsManager,
    private val signalHandler: SignalEventHandler,
    private val bluetoothHandler: BluetoothEventHandler,
    private val signalUserSettingsRepositoryImpl: SignalUserSettingsRepositoryImpl,
) : ViewModel() {
    val state = createStateFlow(
        signalUserSettingsRepositoryImpl,
        settingsManager,
    )

    init {
        //TODO костыль - избежать в будующем
        // Инициализируем настройки сигналов при создании ViewModel
        viewModelScope.launch {
            signalUserSettingsRepositoryImpl.initDefaults()
        }
    }

    private fun createStateFlow(
        chartSettingsRepository: SignalUserSettingsRepositoryImpl,
        settingsManager: SettingsManager,
    ): StateFlow<SettingsState> {

        val chartSettingsFlow = chartSettingsRepository.observe()
            .map { it.userChartSettingsMapToUI() }

        val bluetoothFlow = combine(
            settingsManager.isEnabledChecked(),
            settingsManager.getBluetoothMask()
        ) { isEnabled, mask -> WirelessBluetoothMask(isEnabled, mask) }


        return combine(
            chartSettingsFlow,
            bluetoothFlow,
        ) { chartSettings, bluetooth ->
            SettingsState(
                chartSettings = chartSettings,
                wirelessBluetoothMask = bluetooth,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SettingsStateDefaults.getDefault()
        )
    }

    fun onEvents(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SignalEvent -> signalHandler.handle(event)
                is BluetoothEvent -> bluetoothHandler.handle(event)
            }
        }
    }
}