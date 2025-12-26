package com.psis.transfer.chart.domain.usecase

import com.psis.transfer.chart.data.SignalDefinitionsRepositoryImpl
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.chart.domain.model.Line
import com.psis.transfer.chart.domain.model.Point
import com.psis.transfer.chart.domain.model.SignalDefinition
import com.psis.transfer.chart.domain.model.TimeRange
import com.psis.transfer.chart.domain.toTimestampMs
import com.psis.transfer.protocol.data.repository.ElevatorArchiveBufferRepository
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min


class ObserveListLineUseCase @Inject constructor(
    private val stateRepository: ElevatorStateRepository,
    private val archiveRepository: ElevatorArchiveBufferRepository,
    private val signalDefinitionsRepositoryImpl: SignalDefinitionsRepositoryImpl,
) {
    operator fun invoke(
        versionFlow: Flow<Int>,
        rangeFlow: Flow<TimeRange?>,
        maxGapDistance: Long = 602_000L,
        useState: Boolean = true,
    ): Flow<List<Line>> = combine(
        archiveRepository.observe(),
        stateRepository.observeElevatorState(),
        signalDefinitionsRepositoryImpl.observe(versionFlow),
        rangeFlow,
    ) { archive, state, settings, range ->
        if (range == null) return@combine emptyList()
        val temp = if (useState) (archive + listOf(state)) else archive
        val timestampSignal = settings.find { it.name.contains("time", ignoreCase = true) }
            ?: return@combine emptyList()
        val millisSignal = settings.find { it.name.contains("ms", ignoreCase = true) }
            ?: return@combine emptyList()

        temp
            .selectRangeWithNeighbors(range, timestampSignal, millisSignal)
            .removeDuplicates()
            .sortTime(settings)
            .tryCreateVirtualPoint(
                targetTime = range.start.toTimestampMs(),
                distance = maxGapDistance,
                timestampSignal = timestampSignal,
                millisSignal = millisSignal,
            )
            .sortTime(settings)
            .tryCreateVirtualPoint(
                targetTime = range.end.toTimestampMs(),
                distance = maxGapDistance,
                timestampSignal = timestampSignal,
                millisSignal = millisSignal,
            )
            .sortTime(settings)
            .selectRange(
                startTimeMs = range.start.toTimestampMs(),
                endTimeMs = range.end.toTimestampMs(),
                timestampSignal = timestampSignal,
                millisSignal = millisSignal,
            )
//            .logPackets(tag = "ObserveListLineUseCase", maxPackets = 40)
            .mapToListLine(settings)

    }.flowOn(Dispatchers.Default)

    private fun List<List<Byte>>.tryCreateVirtualPoint(
        targetTime: Long,
        distance: Long,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition,
    ): List<List<Byte>> {
        val searchIndex = binarySearchBy(targetTime) { frame ->
            extractTimeFromFrame(frame, timestampSignal, millisSignal) ?: Long.MIN_VALUE
        }

        val virt = when {
            searchIndex >= 0 -> null // если положительное то ничиге не добавляем
            else -> {
                val insertionPoint = -(searchIndex + 1)
                tryCreateVirtualPoint(
                    frames = this,
                    distance = distance,
                    targetTime = targetTime,
                    insertionPoint = insertionPoint,
                    timestampSignal = timestampSignal,
                    millisSignal = millisSignal,
                )

            }
        }
        val result = mutableListOf<List<Byte>>()
        virt?.let { result.add(it) }
        result.addAll(this)

        return result
    }


    private fun tryCreateVirtualPoint(
        insertionPoint: Int,
        distance: Long,
        targetTime: Long,
        frames: List<List<Byte>>,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition,
    ): List<Byte>? {
        if (insertionPoint == 0 || insertionPoint >= frames.size) return null

        val leftNeighbor = frames[insertionPoint - 1]
        val rightNeighbor = frames[insertionPoint]

        val leftTime =
            extractTimeFromFrame(leftNeighbor, timestampSignal, millisSignal) ?: return null
        val rightTime =
            extractTimeFromFrame(rightNeighbor, timestampSignal, millisSignal) ?: return null

        // Проверяем, что targetTime действительно между соседями
        if (targetTime <= leftTime || targetTime >= rightTime) return null

        // Проверяем расстояние между соседями
        val gapSize = rightTime - leftTime
        if (gapSize >= distance) return null

        // Создаем копию с заменой времени на targetTime
        return createFrameWithNewTime(
            sourceFrame = leftNeighbor,
            newTime = targetTime,
            timestampSignal = timestampSignal,
            millisSignal = millisSignal
        )
    }

    private fun createFrameWithNewTime(
        sourceFrame: List<Byte>,
        newTime: Long,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition
    ): List<Byte> {
        // Разбиваем newTime на секунды и миллисекунды
        val newTimestamp = (newTime / 1000L).toInt()  // секунды
        val newMillis = (newTime % 1000L).toInt()     // миллисекунды

        var updatedFrame = SignalUtils.updateSignalInByteData(
            byteData = sourceFrame,
            offset = timestampSignal.offset,
            type = timestampSignal.type,
            value = newTimestamp
        )

        // Обновляем миллисекунды
        updatedFrame = SignalUtils.updateSignalInByteData(
            byteData = updatedFrame,
            offset = millisSignal.offset,
            type = millisSignal.type,
            value = newMillis
        )

        return updatedFrame
    }

    private fun List<List<Byte>>.removeDuplicates(): List<List<Byte>> =
        distinct()

    private fun List<List<Byte>>.sortTime(settings: List<SignalDefinition>): List<List<Byte>> {
        if (this.isEmpty()) return emptyList()
        // Находим сигналы времени один раз
        val timestampSignal = settings.find { it.name.contains("time", ignoreCase = true) }
            ?: return emptyList()
        val millisSignal = settings.find { it.name.contains("ms", ignoreCase = true) }
            ?: return emptyList()

        return this.sortedBy { frame ->
            try {
                // Извлекаем время из фрейма
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

                totalTimeMs
            } catch (_: Exception) {
                Long.MAX_VALUE // Фреймы с ошибкой отправляем в конец
            }
        }
    }

    fun List<List<Byte>>.selectRangeWithNeighbors(
        range: TimeRange?,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition,
    ): List<List<Byte>> {
        if (this.isEmpty()) return emptyList()
        if (range == null) return emptyList()

        // Извлекаем границы диапазона в миллисекундах
        val startTimeMs = range.start.toTimestampMs()
        val endTimeMs = range.end.toTimestampMs()

        return this.selectRange(
            startTimeMs = startTimeMs,
            endTimeMs = endTimeMs,
            timestampSignal = timestampSignal,
            millisSignal = millisSignal,
        ) + this.searchNeighbor(
            timeMs = startTimeMs,
            timestampSignal = timestampSignal,
            millisSignal = millisSignal
        ) + this.searchNeighbor(
            timeMs = endTimeMs,
            timestampSignal = timestampSignal,
            millisSignal = millisSignal
        )
    }

    /**
     * Выбирает фреймы, попадающие в заданный диапазон времени
     * Работает с отсортированным списком, использует бинарный поиск
     */
    fun List<List<Byte>>.selectRange(
        startTimeMs: Long,
        endTimeMs: Long,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition
    ): List<List<Byte>> {

        val result = mutableListOf<List<Byte>>()
        for (frame in this) {
            val time = extractTimeFromFrame(frame, timestampSignal, millisSignal)
                ?: continue  // Пропускаем фреймы, где не удалось извлечь время

            if (time in min(startTimeMs, endTimeMs)..max(startTimeMs, endTimeMs)) {
                result.add(frame)
            }
        }
        // Возвращаем подсписок
        return result
    }


    /**
     * Ищет ближайшие фреймы к границам диапазона
     * Проходит по всем фреймам, так как список не отсортирован
     */
    fun List<List<Byte>>.searchNeighbor(
        timeMs: Long,
        timestampSignal: SignalDefinition,
        millisSignal: SignalDefinition,
    ): List<List<Byte>> {
        if (isEmpty()) return emptyList()

        var exactMatch: List<Byte>? = null
        var leftNeighbor: List<Byte>? = null
        var rightNeighbor: List<Byte>? = null

        var minLeftDistance = Long.MAX_VALUE  // минимальное расстояние слева
        var minRightDistance = Long.MAX_VALUE // минимальное расстояние справа

        for (frame in this) {
            val frameTimeMs = try {
                // Извлекаем время из фрейма
                val seconds = SignalUtils.extractSignalValueFromByteData(
                    frame,
                    timestampSignal.offset,
                    timestampSignal.type
                ).toLong()

                val millis = SignalUtils.extractSignalValueFromByteData(
                    frame,
                    millisSignal.offset,
                    millisSignal.type
                )

                seconds * 1000L + millis
            } catch (_: Exception) {
                continue
            }

            // Проверяем точное совпадение
            if (frameTimeMs == timeMs) {
                exactMatch = frame
            }

            // Ищем ближайший слева (frameTimeMs < timeMs)
            if (frameTimeMs < timeMs) {
                val distance = timeMs - frameTimeMs
                if (distance < minLeftDistance) {
                    minLeftDistance = distance
                    leftNeighbor = frame
                }
            }

            // Ищем ближайший справа (frameTimeMs > timeMs)
            if (frameTimeMs > timeMs) {
                val distance = frameTimeMs - timeMs
                if (distance < minRightDistance) {
                    minRightDistance = distance
                    rightNeighbor = frame
                }
            }
        }

        // Формируем результат
        return buildList {
            if (leftNeighbor != null) add(leftNeighbor)
            if (exactMatch != null) add(exactMatch)
            if (rightNeighbor != null) add(rightNeighbor)
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

    private fun List<List<Byte>>.mapToListLine(
        settings: List<SignalDefinition>,
    ): List<Line> {
        if (this.isEmpty()) return emptyList()

        // Находим сигналы времени один раз
        val timestampSignal = settings.find { it.name.contains("time", ignoreCase = true) }
            ?: return emptyList()
        val millisSignal = settings.find { it.name.contains("ms", ignoreCase = true) }
            ?: return emptyList()

        // Создаем карту для накопления точек по имени сигнала
        val pointsBySignal = mutableMapOf<String, MutableList<Point>>()
        val descriptionBySignal = mutableMapOf<String, String>()

        // Обрабатываем все фреймы
        for (frame in this) {
            try {
                // Извлекаем время для текущего фрейма
                val timestamp = SignalUtils.extractSignalValueFromByteData(
                    frame,
                    timestampSignal.offset,
                    timestampSignal.type
                ).toLong()

                val millis = SignalUtils.extractSignalValueFromByteData(
                    frame,
                    millisSignal.offset,
                    millisSignal.type
                )

                // Для каждого сигнала (кроме времени и миллисекунд) извлекаем значение
                for (signal in settings) {
                    // Пропускаем сигналы времени
                    if (signal.name.contains("time", ignoreCase = true) ||
                        signal.name.contains("ms", ignoreCase = true)
                    ) {
                        continue
                    }

                    try {
                        val value = SignalUtils.extractSignalValueFromByteData(
                            frame,
                            signal.offset,
                            signal.type
                        )

                        // Создаем точку
                        val point = Point(
                            time = timestamp,
                            millis = millis,
                            value = value
                        )

                        // Добавляем точку в коллекцию для этого сигнала
                        val pointsList = pointsBySignal.getOrPut(signal.name) {
                            mutableListOf()
                        }
                        pointsList.add(point)

                        // Сохраняем описание сигнала (один раз)
                        descriptionBySignal.putIfAbsent(signal.name, signal.comment)

                    } catch (_: Exception) {
                        // Пропускаем ошибки для отдельных сигналов
                    }
                }
            } catch (_: Exception) {
                // Пропускаем фреймы с ошибками
            }
        }

        // Создаем линии из накопленных точек
        return pointsBySignal.map { (signalName, points) ->
            Line(
                name = signalName,
                description = descriptionBySignal[signalName] ?: "",
                points = points.sortedBy { it.time * 1000L + it.millis }
            )
        }
    }

}
