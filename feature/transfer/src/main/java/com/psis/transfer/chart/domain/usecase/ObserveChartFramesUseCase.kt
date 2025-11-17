package com.psis.transfer.chart.domain.usecase

import android.util.Log
import com.psis.transfer.chart.data.ChartSettingsRepository
import com.psis.transfer.chart.domain.ChartConfigRepository
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.chart.domain.model.ChartSignalsConfig
import com.psis.transfer.chart.domain.model.SignalColor
import com.psis.transfer.chart.domain.model.SignalSettings
import com.psis.transfer.protocol.data.repository.ElevatorArchiveBufferRepository
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import com.psis.transfer.protocol.domain.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min


data class ChartFrame(
    val timestampSeconds: Long,
    val millis: Int,
    val signals: List<SignalPoint>,
)

data class SignalPoint(
    val name: String,
    val value: Int,
    val color: SignalColor? = null
)

//TODO смещение  отрицательно это ближе к 1970 положительное ближе к текущему
class ObserveChartFramesUseCase @Inject constructor(
    private val stateRepository: ElevatorStateRepository,
    private val archiveRepository: ElevatorArchiveBufferRepository,
    private val chartConfigRepository: ChartConfigRepository,
    private val chartSettingsRepository: ChartSettingsRepository,
) {
    operator fun invoke(): Flow<List<ChartFrame>> = visibleFramesFlow

    fun observeRawFramesFlow() = rawFramesFlow

    private val rawFramesFlow = combine(
        stateRepository.observeElevatorState(),
        archiveRepository.observe(),
        chartSettingsRepository.observe()
    ) { state, archive, settings ->
        (archive + listOf(state))
            .removeDuplicates()
//            .removeZeroBlocks()
            .decodeChartFrames(settings.config)
//            .logDebug()
            .sortTime()
            .logDebug("SortedFrames", to = 5)
    }.flowOn(Dispatchers.Default)


    private val visibleFramesFlow = combine(
        rawFramesFlow,
        chartConfigRepository.observe(),
    ) { data, config ->
        data
            .clipToVisibleRange(
                stepCount = config.stepCount - 10,
                offset = config.offset.toInt()
            )
//            .logDebug("VisibleRange", to = 20)
            .ensureStartPoint(
                fullData = data,
                stepCount = config.stepCount,
            )
            .logDebug("BackfillStart", to = 20)
    }

    private fun ChartFrame.timeMs(): Long = timestampSeconds * 1000L + millis

    private fun List<ChartFrame>.clipToVisibleRange(
        stepCount: Int,
        offset: Int,
    ): List<ChartFrame> {
        if (isEmpty()) return emptyList()

        val latestFrame = maxByOrNull { it.timeMs() } ?: return emptyList()

        val range = calcRange(
            anchor = latestFrame.timeMs(),
            stepMs = stepCount * 1000L,
            offsetMs = offset * 1000L
        )


//        return clipToRange(range)
//            .logDebug("clipToRange", to = 20)
//            .ensurePointAt(
//                rangeEnd = range.last,
//                frame = minAfterRange
//            )

        // TODO как будто тут можно схитрить и не делать хуйню
        if (any { it.timeMs() == range.last }) return this.clipToRange(range)

        val minAfterRange = this
            .filter { it.timeMs() > range.last }
            .minByOrNull { it.timeMs() } ?: return emptyList()

        val newFrame = minAfterRange.copy(
            timestampSeconds = range.last / 1000,
            millis = (range.last % 1000).toInt()
        )

        return this.clipToRange(range) + newFrame
    }

    private fun List<ChartFrame>.clipToRange(range: LongRange): List<ChartFrame> =
        filter { it.timeMs() in range }


    private fun List<ChartFrame>.ensurePointAt(
        rangeEnd: Long,
        frame: ChartFrame
    ): List<ChartFrame> {
        if (any { it.timeMs() == rangeEnd }) return this

        val newFrame = frame.copy(
            timestampSeconds = rangeEnd / 1000,
            millis = (rangeEnd % 1000).toInt()
        )

        return this + newFrame
    }

    private fun List<ChartFrame>.ensureStartPoint(
        fullData: List<ChartFrame>,
        stepCount: Int,
    ): List<ChartFrame> {
        if (isEmpty()) return emptyList()

        val minPoint = this.minByOrNull { it.timeMs() } ?: return this
        val maxPoint = this.maxByOrNull { it.timeMs() } ?: return this


        val segmentStartMs = maxPoint.timeMs() - stepCount * 1000

        // Если существет точка на начале отрезка возвращаем отрезок
        if (this.any { it.timeMs() == segmentStartMs }) return this

        // Иначе проверяем на обрыв и если все ок генерим точку для отрезка
        val brakeRange = calcRange(
            anchor = minPoint.timeMs(),
            stepMs = REWRITE_INTERVAL_ARCHIVE,
        )

        val templatePoint = fullData
            .filter { it.timeMs() in brakeRange }
            .filter { it !== minPoint }
            .maxByOrNull { it.timeMs() }

        if (templatePoint == null) return this

        val newPoint = templatePoint.copy(
            timestampSeconds = segmentStartMs / 1000,
            millis = (segmentStartMs % 1000).toInt()
        )
        return listOf(newPoint) + this
    }


    private fun calcRange(
        anchor: Long,
        stepMs: Long,
        offsetMs: Long = 0L
    ): LongRange {
        val end = anchor + offsetMs
        val start = end - stepMs

        return min(start, end)..max(start, end)
    }


    private fun List<List<Byte>>.removeDuplicates(): List<List<Byte>> =
        this.distinct()

    private fun List<List<Byte>>.removeZeroBlocks(): List<List<Byte>> =
        this.filter { block -> block.any { it != 0.toByte() } }


    private fun List<ChartFrame>.sortTime(): List<ChartFrame> =
        sortedWith(compareBy<ChartFrame> { it.timestampSeconds }.thenBy { it.millis })


    private fun List<ChartFrame>.logDebug(
        tag: String = "ChartFrame",
        from: Int = 0,          // с какого элемента начать
        to: Int? = 5,        // до какого элемента (не включая)
        fromEnd: Boolean = true
    ): List<ChartFrame> {

        val snapshot = this.toList()
        if (snapshot.isEmpty()) {
            Log.d(tag, "List is empty")
            return this
        }

        val size = snapshot.size

        // Определяем рабочий диапазон индексов
        val start = if (fromEnd) (size - (to ?: size)).coerceAtLeast(0)
        else from.coerceAtLeast(0)

        val end = if (fromEnd) (size - from).coerceAtMost(size)
        else (to ?: size).coerceAtMost(size)

        if (start >= end || start !in 0..size || end !in 0..size) {
            Log.d(tag, "Invalid range: start=$start end=$end size=$size")
            return this
        }

        val list = snapshot.subList(start, end)

        val sb = StringBuilder("Items: ${list.size}\n")

        list.forEach { item ->
            val formattedTime =
                DateTimeUtils.formatTimeWithMillis(item.timestampSeconds, item.millis)
            val values = item.signals.joinToString { dop -> "${dop.name}=${dop.value}" }
            sb.append("$formattedTime values=[$values]\n")
        }

        Log.d(tag, sb.toString())

        return this
    }


    private fun List<List<Byte>>.decodeChartFrames(config: ChartSignalsConfig): List<ChartFrame> {
        val timestampSignal = config.timestampSignal ?: return emptyList()
        val millisSignal = config.millisSignal ?: return emptyList()

        return mapNotNull { packet ->
            runCatching {
                packet.decodeChartFrame(
                    timestampSignal,
                    millisSignal,
                    config.signals
                )
            }.getOrNull()
        }
    }

    private fun List<Byte>.decodeChartFrame(
        timestampSignal: SignalSettings,
        millisSignal: SignalSettings,
        signals: List<SignalSettings>
    ): ChartFrame {
//        if (this.size < MIN_HEADER_SIZE) return

        val timestamp = SignalUtils.extractSignalValueFromByteData(
            this,
            timestampSignal.offset,
            timestampSignal.type
        ).toLong()

        val millis = SignalUtils.extractSignalValueFromByteData(
            this,
            millisSignal.offset,
            millisSignal.type
        )

        val values = signals.map { signal ->
            SignalPoint(
                name = signal.name,
                value = SignalUtils.extractSignalValueFromByteData(
                    this,
                    signal.offset,
                    signal.type
                ),
                color = signal.color   // если цвет в конфиге
            )
        }

        return ChartFrame(
            timestampSeconds = timestamp,
            millis = millis,
            signals = values
        )
    }


    companion object {
        private const val MIN_HEADER_SIZE = 6
        private const val REWRITE_INTERVAL_ARCHIVE = 602_000L
    }
}