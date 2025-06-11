package com.example.transfer.chart.data

import com.example.transfer.chart.domain.model.SignalColor
import com.example.transfer.chart.domain.model.ChartSettings
import com.example.transfer.chart.domain.ChartSettingsDefaults
import com.example.transfer.chart.domain.model.SignalSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartSettingsRepository @Inject constructor() {

    private val _chartSettings = MutableStateFlow(ChartSettingsDefaults.getDefault())
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
                signals = updateSignal(current.signals, signalId) { it.copy(color = color) }
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
    ): List<SignalSettings> =
        signals.map { signal -> if (signal.id == signalId) transform(signal) else signal }
}