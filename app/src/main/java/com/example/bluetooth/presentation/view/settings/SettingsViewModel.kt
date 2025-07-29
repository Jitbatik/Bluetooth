package com.example.bluetooth.presentation.view.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.domain.SettingsManager
import com.example.bluetooth.presentation.view.settings.model.BluetoothEvent
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.SettingsState
import com.example.bluetooth.presentation.view.settings.model.SignalEvent
import com.example.bluetooth.presentation.view.settings.model.WirelessBluetoothMask
import com.example.bluetooth.presentation.view.settings.utils.chartSettingsMapToUI
import com.example.transfer.chart.data.ChartSettingsRepository
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
    chartSettingsRepository: ChartSettingsRepository,
    private val signalHandler: SignalEventHandler,
    private val bluetoothHandler: BluetoothEventHandler,
) : ViewModel() {
    val state = createStateFlow(
        chartSettingsRepository,
        settingsManager,
    )

    private fun createStateFlow(
        chartSettingsRepository: ChartSettingsRepository,
        settingsManager: SettingsManager,
    ): StateFlow<SettingsState> {
        val chartSettingsFlow = chartSettingsRepository.observe()
            .map { it.chartSettingsMapToUI() }

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