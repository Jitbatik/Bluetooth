package com.example.bluetooth

import com.example.bluetooth.model.ChartSettings
import com.example.bluetooth.model.SignalColor
import com.example.bluetooth.model.SignalSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ChartSettingsRepository @Inject constructor() {

    private val _chartSettings = MutableStateFlow(initialChartSettings())
    val chartSettings: StateFlow<ChartSettings> = _chartSettings.asStateFlow()

    fun toggleSignalVisibility(signalId: String, isVisible: Boolean) {
        _chartSettings.update { current ->
            current.copy(
                signals = updateSignal(current.signals, signalId) {
                    it.copy(isVisible = isVisible)
                }
            )
        }
    }

    fun changeSignalColor(signalId: String, color: SignalColor) {
        _chartSettings.update { current ->
            current.copy(
                signals = updateSignal(current.signals, signalId) {
                    it.copy(color = color)
                }
            )
        }
    }

    fun makeAllSignalsVisible() {
        _chartSettings.update { current ->
            current.copy(signals = current.signals.map { it.copy(isVisible = true) })
        }
    }

    private fun updateSignal(
        signals: List<SignalSettings>,
        signalId: String,
        transform: (SignalSettings) -> SignalSettings
    ): List<SignalSettings> {
        return signals.map { signal ->
            if (signal.id == signalId) transform(signal) else signal
        }
    }

    private fun initialChartSettings(): ChartSettings {
        return ChartSettings(
            title = "Параметры графика",
            description = "Настройте отображение сигналов на графике параметров",
            signals = listOf(
                SignalSettings("speed", "Скорость", true, SignalColor(255, 0, 0)),       // Красный
                SignalSettings("acceleration", "Ускорение", true, SignalColor(0, 0, 255)), // Синий
                SignalSettings("position", "Положение", true, SignalColor(0, 255, 0)),     // Зеленый
                SignalSettings("temperature", "Температура", false, SignalColor(255, 255, 0)) // Желтый
            )
        )
    }
}
