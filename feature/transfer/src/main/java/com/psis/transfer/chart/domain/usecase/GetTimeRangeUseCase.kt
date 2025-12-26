package com.psis.transfer.chart.domain.usecase

import android.util.Log
import com.psis.transfer.chart.data.SignalDefinitionsRepositoryImpl
import com.psis.transfer.chart.domain.ChartConfigRepository
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.chart.domain.millisecondsToDateTimeData
import com.psis.transfer.chart.domain.model.ChartConfig
import com.psis.transfer.chart.domain.model.SignalDefinition
import com.psis.transfer.chart.domain.model.TimeRange
import com.psis.transfer.protocol.data.repository.ElevatorArchiveBufferRepository
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

class GetTimeRangeUseCase @Inject constructor(
    private val stateRepository: ElevatorStateRepository,
    private val archiveRepository: ElevatorArchiveBufferRepository,
    private val signalDefinitionsRepositoryImpl: SignalDefinitionsRepositoryImpl,
    private val chartConfigRepository: ChartConfigRepository,
) {

    operator fun invoke(
        versionFlow: Flow<Int>,
        useState: Boolean = true,
    ): Flow<TimeRange?> = combine(
        stateRepository.observeElevatorState(),
        archiveRepository.observe(),
        signalDefinitionsRepositoryImpl.observe(versionFlow),
        chartConfigRepository.observe(),
    ) { state, archive, settings, config ->
        val frames = if (useState) (archive + listOf(state)) else archive
        calculateSmartTimeRange(
            frames = frames,
            settings = settings,
            config = config,
        )
    }.flowOn(Dispatchers.Default)

    // TODO Вспомогательная функция для извлечения времени (должна быть согласована с sortTime) вынести в объект???
    private fun extractTimeFromFrame(
        frame: List<Byte>,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition,
    ): Long {

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

    private fun calculateSmartTimeRange(
        frames: List<List<Byte>>,
        settings: List<SignalDefinition>,
        config: ChartConfig,
    ): TimeRange? {
        if (frames.isEmpty()) return null

        val timestampSignal = settings.find { it.name.contains("time", ignoreCase = true) }
            ?: return null
        val millisSignal = settings.find { it.name.contains("ms", ignoreCase = true) }
            ?: return null

        var minTimestampMs: Long? = null
        var maxTimestampMs: Long? = null

        for (frame in frames) {
            try {
                val timeMs = extractTimeFromFrame(
                    frame,
                    timestampSignal,
                    millisSignal
                )

                // Обновляем минимальное время
                minTimestampMs = when {
                    minTimestampMs == null -> timeMs
                    else -> minOf(minTimestampMs, timeMs)
                }

                // Обновляем максимальное время
                maxTimestampMs = when {
                    maxTimestampMs == null -> timeMs
                    else -> maxOf(maxTimestampMs, timeMs)
                }
            } catch (e: Exception) {
                Log.d(
                    "GetTimeRangeUseCase",
                    "Ошибка при извлечении времени из фрейма: ${e.message}"
                )
            }
        }

        val minTime = minTimestampMs ?: return null
        val maxTime = maxTimestampMs ?: return null

        val offsetMs = (config.offset * 1000f).roundToLong().coerceIn(minTime - maxTime, 0)

        val endTimeMs = maxTime + offsetMs
        val startTimeMs = endTimeMs - (config.stepCount * 1000f).roundToLong()

        return TimeRange(
            start = millisecondsToDateTimeData(min(endTimeMs, startTimeMs)),
            end = millisecondsToDateTimeData(max(endTimeMs, startTimeMs)),
        )
    }
}