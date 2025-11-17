package com.psis.transfer.chart.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.psis.transfer.chart.data.ChartSettingsRepository
import com.psis.transfer.chart.domain.ChartConfigRepository
import com.psis.transfer.chart.domain.SignalUtils
import com.psis.transfer.chart.domain.model.ChartConfig
import com.psis.transfer.chart.domain.model.TimeRange
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import com.psis.transfer.protocol.domain.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalTime
import javax.inject.Inject



@RequiresApi(Build.VERSION_CODES.O)
fun LocalTime.toMsOfDay(): Long =
    toSecondOfDay() * 1000L + nano / 1_000_000

@RequiresApi(Build.VERSION_CODES.O)
fun msToLocalTime(ms: Long): LocalTime =
    LocalTime.ofNanoOfDay(ms * 1_000_000)


class TimeRangeManager @Inject constructor(
    private val chartConfigRepository: ChartConfigRepository
) {
    private val current: ChartConfig
        get() = chartConfigRepository.observe().value

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateStart(local: LocalTime) {
        val startMs = local.toMsOfDay()
        val range = current.timeRange ?: TimeRange(startMs, startMs)
        chartConfigRepository.updateTimeRange(range.copy(startMs = startMs))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateEnd(local: LocalTime) {
        val endMs = local.toMsOfDay()
        val range = current.timeRange ?: TimeRange(endMs, endMs)
        chartConfigRepository.updateTimeRange(range.copy(endMs = endMs))
    }
}


//TODO нужно сделать время диапазоном
class ObserveTimeUseCase @Inject constructor(
    private val stateRepository: ElevatorStateRepository,
    private val settingsRepository: ChartSettingsRepository,
    private val chartConfigRepository: ChartConfigRepository,
) {
    operator fun invoke(): Flow<String> = currentTime


    private val currentTime = combine(
        settingsRepository.observe(),
        stateRepository.observeElevatorState()
    ) { settings, byteDataList ->
        if (byteDataList.isEmpty()) {
            return@combine ""
        }
        val timestampSignal = settings.config.timestampSignal
        val millisSignal = settings.config.millisSignal

        if (timestampSignal == null || millisSignal == null) return@combine ""

        val timestamp = SignalUtils.extractSignalValueFromByteData(
            byteDataList,
            timestampSignal.offset,
            timestampSignal.type
        ).toLong()
        val millis = SignalUtils.extractSignalValueFromByteData(
            byteDataList,
            millisSignal.offset,
            millisSignal.type
        )
        val formatted = DateTimeUtils.formatTimeWithMillis(timestamp, millis)
//
//            Log.d("ObserveTimeUseCase", "🕒 Извлечено время: timestamp=$timestamp, millis=$millis → $formatted")
        formatted
    }

}