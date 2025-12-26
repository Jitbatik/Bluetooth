package com.psis.transfer.chart.data

import com.psis.transfer.chart.domain.SignalUserSettingsRepository
import com.psis.transfer.chart.domain.model.SignalColor
import com.psis.transfer.chart.domain.model.SignalUserSettings
import com.psis.transfer.xmlfileforchart.SignalSettingsParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalUserSettingsRepositoryImpl @Inject constructor(
    private val xmlReader: SignalSettingsParser
) : SignalUserSettingsRepository {
    private val lock = Mutex()

    private var cachedDefinitions: List<SignalUserSettings> = emptyList()
    private var isInitialized = false
    private val _settings = MutableStateFlow(cachedDefinitions)
    override fun observe(): Flow<List<SignalUserSettings>> = _settings.asStateFlow()

    override suspend fun updateVisibility(name: String, visible: Boolean) {
        lock.withLock {
            val updated = cachedDefinitions.map { settings ->
                if (settings.name == name) {
                    settings.copy(isVisible = visible)
                } else {
                    settings
                }
            }
            cachedDefinitions = updated
            _settings.value = updated
        }
    }

    override suspend fun updateColor(name: String, color: SignalColor) {
        lock.withLock {
            val updated = cachedDefinitions.map { settings ->
                if (settings.name == name) {
                    settings.copy(color = color)
                } else {
                    settings
                }
            }
            cachedDefinitions = updated
            _settings.value = updated
        }
    }


    override suspend fun makeAllSignalsVisible() {
        lock.withLock {
            val updated = cachedDefinitions.map { settings ->
                settings.copy(isVisible = true)
            }
            cachedDefinitions = updated
            _settings.value = updated
        }
    }

    override suspend fun initDefaults() {
        lock.withLock {
            if (isInitialized) return@withLock cachedDefinitions

            val parsed = xmlReader.parse("l.xml")

            // Фильтруем исключенные сигналы
            val filtered = parsed.filter {
                val name = it.name.lowercase()
                !name.contains("time") && !name.contains("ms")
            }

            // Задаем цвета с чередованием 8 цветов
            val defaultColors = listOf(
                SignalColor(255, 100, 100),    // Красный
                SignalColor(100, 255, 100),    // Зеленый
                SignalColor(100, 100, 255),    // Синий
                SignalColor(255, 255, 100),    // Желтый
                SignalColor(255, 150, 50),     // Оранжевый
                SignalColor(200, 100, 255),    // Фиолетовый
                SignalColor(100, 255, 255),    // Бирюзовый
                SignalColor(255, 100, 200),    // Розовый
            )

            val mapped = filtered.mapIndexed { index, signal ->
                val colorIndex = index % defaultColors.size

                SignalUserSettings(
                    name = signal.name,
                    isVisible = true,
                    color = defaultColors[colorIndex]
                )
            }

            cachedDefinitions = mapped
            _settings.value = mapped
            isInitialized = true
        }
    }
}