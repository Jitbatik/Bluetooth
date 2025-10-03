package com.psis.transfer.chart.data

import com.psis.transfer.chart.domain.ChartSettingsDefaults
import com.psis.transfer.chart.domain.model.ChartSettings
import com.psis.transfer.chart.domain.model.ChartSignalsConfig
import com.psis.transfer.chart.domain.model.SignalColor
import com.psis.transfer.chart.domain.model.SignalSettings
import com.psis.transfer.xmlfileforchart.SignalSettingsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartSettingsRepository @Inject constructor(
    private val xmlReader: SignalSettingsParser
) {
    private val _chartSettings = MutableStateFlow(ChartSettingsDefaults.getDefault())

    private val lock = Mutex()

    fun observe(): StateFlow<ChartSettings> = _chartSettings.asStateFlow()

    private val defaultColors = listOf(
        SignalColor(255, 0, 0),    // Красный
        SignalColor(0, 255, 0),    // Зелёный
        SignalColor(0, 0, 255),    // Синий
        SignalColor(255, 255, 0),  // Жёлтый
        SignalColor(255, 0, 255),  // Магента
        SignalColor(0, 255, 255),  // Циан
        SignalColor(255, 165, 0),  // Оранжевый
        SignalColor(128, 0, 128)   // Фиолетовый
    )

    suspend fun initIfNeeded(version: Int) {
        withContext(Dispatchers.IO) {
            lock.withLock {
                val fileName = resolveFileName(version)
                val allSignals = xmlReader.parse(fileName)

                val timestamp = allSignals.find { it.name == "Time" }
                    ?: error("Signal 'Time' not found")
                val millis = allSignals.find { it.name == "ms" }
                    ?: error("Signal 'ms' not found")

                val userSignals = allSignals
                    .filter { it.name != "Time" && it.name != "ms" }
                    .mapIndexed { index, signal ->
                        // назначаем цвет по циклу
                        val color = defaultColors[index % defaultColors.size]
                        signal.copy(color = color)
                    }

                _chartSettings.value = ChartSettings(
                    title = "Отображение графика",
                    description = "Настройте отображение сигналов на графике состояний",
                    config = ChartSignalsConfig(
                        timestampSignal = timestamp,
                        millisSignal = millis,
                        signals = userSignals
                    )
                )
            }
        }
    }

    fun getVersionSignalInfo(): Pair<Int, String> {
        val versionSignalName = "Ver"
        val fileName = "l.xml"
        val versionField = xmlReader
            .parse(fileName)
            .find { it.name == versionSignalName }
            ?: error("Signal '$versionSignalName' not found in $fileName")

        return Pair(versionField.offset, versionField.type)
    }
    private fun resolveFileName(version: Int): String = when (version) {
        0 -> "ver0.xml"
        1 -> "ver1.xml"
        2 -> "l.xml"
        else -> error("Unknown version: $version")
    }



    fun toggleSignalVisibility(signalId: String, isVisible: Boolean) {
        _chartSettings.update { current ->
            val updatedSignals = updateSignal(current.config.signals, signalId) {
                it.copy(isVisible = isVisible)
            }
            current.copy(config = current.config.copy(signals = updatedSignals))
        }
    }


    fun changeSignalColor(signalId: String, color: SignalColor) {
        _chartSettings.update { current ->
            val updated = updateSignal(current.config.signals, signalId) {
                it.copy(color = color)
            }
            current.copy(config = current.config.copy(signals = updated))
        }
    }

    fun makeAllSignalsVisible() {
        _chartSettings.update { current ->
            val updated = current.config.signals.map { it.copy(isVisible = true) }
            current.copy(config = current.config.copy(signals = updated))
        }
    }

    private fun updateSignal(
        signals: List<SignalSettings>,
        signalName: String,
        transform: (SignalSettings) -> SignalSettings
    ): List<SignalSettings> =
        signals.map { signal -> if (signal.name == signalName) transform(signal) else signal }
}