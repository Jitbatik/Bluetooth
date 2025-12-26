package com.psis.transfer.chart.domain.usecase.temp

import com.psis.transfer.chart.data.SignalDefinitionsRepositoryImpl
import com.psis.transfer.chart.domain.ChartConfigRepository
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.chart.domain.model.ChartConfig
import com.psis.transfer.chart.domain.model.SignalDefinition
import com.psis.transfer.chart.domain.model.TimeRange
import com.psis.transfer.chart.domain.toTimestampMs
import com.psis.transfer.protocol.data.repository.ElevatorArchiveBufferRepository
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateTimeRangeUseCase @Inject constructor(
    private val stateRepository: ElevatorStateRepository,
    private val archiveRepository: ElevatorArchiveBufferRepository,
    private val signalDefinitionsRepositoryImpl: SignalDefinitionsRepositoryImpl,
    private val chartConfigRepository: ChartConfigRepository,
) {
    suspend operator fun invoke(
        newTimeRange: TimeRange,
        versionFlow: Flow<Int>,
        useState: Boolean = true,
    ) {
        val startMs = newTimeRange.start.toTimestampMs()
        val endMs = newTimeRange.end.toTimestampMs()
        val durationSeconds = (endMs - startMs) / 1000f

        val currentConfig = chartConfigRepository.observe().first()
        val scale = calculateScaleFromDuration(
            durationSeconds = durationSeconds,
            config = currentConfig
        )

        val offset = combine(
            archiveRepository.observe(),
            stateRepository.observeElevatorState(),
            signalDefinitionsRepositoryImpl.observe(versionFlow),
        ) { archive, state, settings ->
            val temp = if (useState) (archive + listOf(state)) else archive
            val timestampSignal = settings.find { it.name.contains("time", ignoreCase = true) }
                ?: return@combine 0f
            val millisSignal = settings.find { it.name.contains("ms", ignoreCase = true) }
                ?: return@combine 0f

            // Находим максимальное время в temp
            val maxTimeMs = temp.maxOfOrNull { frame ->
                extractTimeFromFrame(frame, timestampSignal, millisSignal) ?: 0L
            } ?: 0L

            (maxTimeMs - endMs) / 1000f
        }.first()

        // Обновляем конфигурацию
        chartConfigRepository.update { config ->
            config.copy(
                scale = scale.coerceIn(config.minScale, config.maxScale),
                offset = offset
            )
        }
    }

    // Вспомогательная функция для извлечения времени (должна быть согласована с sortTime)
    private fun extractTimeFromFrame(
        frame: List<Byte>,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition,
    ): Long? {
        if (frame.isEmpty()) return null
        val timestamp = SignalUtils.extractSignalValueFromByteData(
            frame,
            timestampSignal.offset,
            timestampSignal.type
        ).toLong()

        var totalTimeMs = timestamp * 1000L // конвертируем секунды в миллисекунды

        // Если есть миллисекунды, добавляем их
        millisSignal.let { signal ->
            val millis = SignalUtils.extractSignalValueFromByteData(
                frame,
                signal.offset,
                signal.type
            )
            totalTimeMs += millis
        }

        return totalTimeMs
    }


    private fun calculateScaleFromDuration(durationSeconds: Float, config: ChartConfig): Float {
        // Инвертированная формула из stepCount
        // stepCount = minScalePoint + ((scale - minScale) * (maxScalePoint - minScalePoint) / (maxScale - minScale))
        // Выражаем scale через stepCount (где stepCount = durationSeconds):

        // scale = minScale + (stepCount - minScalePoint) * (maxScale - minScale) / (maxScalePoint - minScalePoint)

        val stepCount = durationSeconds

        return config.minScale + (stepCount - config.minScalePoint) *
                (config.maxScale - config.minScale) /
                (config.maxScalePoint - config.minScalePoint)
    }
}