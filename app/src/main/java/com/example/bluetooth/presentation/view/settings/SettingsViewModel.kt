package com.example.bluetooth.presentation.view.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.ChartSettingsRepository
import com.example.bluetooth.data.SettingsManagerImpl
import com.example.bluetooth.model.ChartSettings
import com.example.bluetooth.model.ChartSettingsUI
import com.example.bluetooth.model.SignalColor
import com.example.bluetooth.model.SignalSettingsUI
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.WirelessNetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManagerImpl,
    private val chartSettingsRepository: ChartSettingsRepository
) : ViewModel() {
    val chartSettingsUI: StateFlow<ChartSettingsUI> = chartSettingsRepository.chartSettings
        .mapChartSettingsRepositoryToUI()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialChartSettings()
        )

    private fun Flow<ChartSettings>.mapChartSettingsRepositoryToUI(): Flow<ChartSettingsUI> {
        return this.map { chartSettings ->
            ChartSettingsUI(
                title = chartSettings.title,
                description = chartSettings.description,
                signals = chartSettings.signals.map { signal ->
                    SignalSettingsUI(
                        id = signal.id,
                        name = signal.name,
                        isVisible = signal.isVisible,
                        color = Color(signal.color.red, signal.color.green, signal.color.blue)
                    )
                }
            )
        }
    }


    private val _wirelessNetworkState = MutableStateFlow(
        WirelessNetworkState(
            isEnabled = settingsManager.isEnabledChecked(),
            mask = settingsManager.getBluetoothMask()
        )
    )
    val wirelessNetworkState: StateFlow<WirelessNetworkState> = _wirelessNetworkState.asStateFlow()

    fun onEvents(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleSignalVisibility -> chartSettingsRepository.toggleSignalVisibility(
                event.signalId,
                event.isVisible
            )

            is SettingsEvent.ChangeSignalColor -> chartSettingsRepository.changeSignalColor(
                event.signalId,
                event.color.toSignalColor()
            )

            is SettingsEvent.MakeAllSignalsVisible -> chartSettingsRepository.makeAllSignalsVisible()

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

    private fun Color.toSignalColor(): SignalColor =
        SignalColor(red = red.toInt(), green = green.toInt(), blue = blue.toInt())

    private fun initialChartSettings(): ChartSettingsUI {
        return ChartSettingsUI(
            title = "Параметры графика",
            description = "Настройте отображение сигналов на графике параметров",
            signals = listOf(
                SignalSettingsUI("speed", "Скорость", true, Color.Red),
                SignalSettingsUI("acceleration", "Ускорение", true, Color.Blue),
                SignalSettingsUI("position", "Положение", true, Color.Green),
                SignalSettingsUI("temperature", "Температура", false, Color.Yellow)
            )
        )
    }
}