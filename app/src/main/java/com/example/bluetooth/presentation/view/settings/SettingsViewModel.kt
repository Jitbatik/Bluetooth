package com.example.bluetooth.presentation.view.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.domain.SettingsManager
import com.example.bluetooth.model.ChartSettingsUI
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
    private val initialChartSettings =
        chartSettingsRepository.chartSettings.value.chartSettingsMapToUI()

    private val initialSettingsState = SettingsState(
        chartSettings = initialChartSettings,
        wirelessBluetoothMask = WirelessBluetoothMask(
            isEnabled = false,
            mask = ""
        ),
        onEvents = ::onEvents
    )

    private val chartSettingsUI: StateFlow<ChartSettingsUI> = chartSettingsRepository.chartSettings
        .map { it.chartSettingsMapToUI() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialChartSettings
        )


    private val _state = combine(
        chartSettingsUI,
        settingsManager.isEnabledChecked(),
        settingsManager.getBluetoothMask()
    ) { chartSettings, isEnable, mask ->
        SettingsState(
            chartSettings = chartSettings,
            wirelessBluetoothMask = WirelessBluetoothMask(
                isEnabled = isEnable,
                mask = mask
            ),
            onEvents = ::onEvents
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = initialSettingsState
    )
    val state: StateFlow<SettingsState> = _state

    fun onEvents(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SignalEvent -> signalHandler.handle(event)
                is BluetoothEvent -> bluetoothHandler.handle(event)
            }
        }
    }
}