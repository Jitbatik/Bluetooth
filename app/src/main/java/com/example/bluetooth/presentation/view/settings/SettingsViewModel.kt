package com.example.bluetooth.presentation.view.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.bluetooth.data.SettingsManagerImpl
import com.example.bluetooth.model.ChartSettings
import com.example.bluetooth.model.SignalSettings
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.WirelessNetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManagerImpl
) : ViewModel() {
    private val _chartSettings = MutableStateFlow(initialChartSettings())
    val chartSettings: StateFlow<ChartSettings> = _chartSettings.asStateFlow()
//    private val _state

    private val _wirelessNetworkState = MutableStateFlow(
        WirelessNetworkState(
            isEnabled = settingsManager.isEnabledChecked(),
            mask = settingsManager.getBluetoothMask()
        )
    )
    val wirelessNetworkState: StateFlow<WirelessNetworkState> = _wirelessNetworkState.asStateFlow()

    fun onEvents(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateSignalVisibility -> updateSignalVisibility(
                event.signalId,
                event.isVisible
            )

            is SettingsEvent.UpdateSignalColor -> updateSignalColor(event.signalId, event.color)
            is SettingsEvent.ShowAllSignals -> showAllSignals()

            is SettingsEvent.UpdateEnabled -> {
                settingsManager.saveEnabledChecked(event.isEnabled)
                _wirelessNetworkState.update { it.copy(isEnabled = event.isEnabled) }
            }
            is SettingsEvent.UpdateMask -> {
                settingsManager.saveBluetoothMask(event.mask)
                _wirelessNetworkState.update { it.copy(mask = event.mask) }
            }
        }
    }

    private fun updateSignalVisibility(signalId: String, isVisible: Boolean) {
        _chartSettings.update { currentSettings ->
            currentSettings.copy(
                signals = currentSettings.signals.map { signal ->
                    if (signal.id == signalId) signal.copy(isVisible = isVisible) else signal
                }
            )
        }
    }

    private fun updateSignalColor(signalId: String, color: Color) {
        _chartSettings.update { currentSettings ->
            currentSettings.copy(
                signals = currentSettings.signals.map { signal ->
                    if (signal.id == signalId) signal.copy(color = color) else signal
                }
            )
        }
    }

    private fun showAllSignals() {
        _chartSettings.update { currentSettings ->
            currentSettings.copy(
                signals = currentSettings.signals.map { it.copy(isVisible = true) }
            )
        }
    }

    private fun initialChartSettings(): ChartSettings {
        return ChartSettings(
            title = "Параметры графика",
            description = "Настройте отображение сигналов на графике параметров",
            signals = listOf(
                SignalSettings("speed", "Скорость", true, Color.Red),
                SignalSettings("acceleration", "Ускорение", true, Color.Blue),
                SignalSettings("position", "Положение", true, Color.Green),
                SignalSettings("temperature", "Температура", false, Color.Yellow)
            )
        )
    }
}