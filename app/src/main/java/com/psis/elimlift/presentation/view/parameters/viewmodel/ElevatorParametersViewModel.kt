package com.psis.elimlift.presentation.view.parameters.viewmodel

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psis.elimlift.presentation.view.parameters.mapper.mapListLineToUiChartList
import com.psis.elimlift.presentation.view.parameters.mapper.toUIParameterDisplayDataFlow
import com.psis.elimlift.presentation.view.parameters.model.Chart
import com.psis.elimlift.presentation.view.parameters.model.ParameterDisplayData
import com.psis.transfer.chart.data.SignalUserSettingsRepositoryImpl
import com.psis.transfer.chart.domain.model.ChartConfig
import com.psis.transfer.chart.domain.model.Line
import com.psis.transfer.chart.domain.model.SignalUserSettings
import com.psis.transfer.chart.domain.model.TimeRange
import com.psis.transfer.chart.domain.usecase.ObserveChartConfigUseCase
import com.psis.transfer.chart.domain.usecase.ObserveElevatorVersionUseCase
import com.psis.transfer.chart.domain.usecase.ObserveListLineUseCase
import com.psis.transfer.chart.domain.usecase.ObservePopDataUseCase
import com.psis.transfer.chart.domain.usecase.UpdateChartOffsetUseCase
import com.psis.transfer.chart.domain.usecase.UpdateChartScaleUseCase
import com.psis.transfer.chart.domain.usecase.GetTimeRangeUseCase
import com.psis.transfer.chart.domain.usecase.temp.UpdateTimeRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElevatorParametersViewModel @Inject constructor(
    observePopDataUseCase: ObservePopDataUseCase,
    observeChartConfigUseCase: ObserveChartConfigUseCase,
    private val updateChartScaleUseCase: UpdateChartScaleUseCase,
    private val updateChartOffsetUseCase: UpdateChartOffsetUseCase,
    observeElevatorVersionUseCase: ObserveElevatorVersionUseCase,
    observeListLineUseCase: ObserveListLineUseCase,
    getTimeRangeUseCase: GetTimeRangeUseCase,
    private val updateTimeRangeUseCase: UpdateTimeRangeUseCase,
    private val signalUserSettingsRepositoryImpl: SignalUserSettingsRepositoryImpl,
) : ViewModel() {
    //TODO Весь блок это ТЕХДОЛГ - тут костыль на костыле
    // todo ========================================================================================
    private val chartConfigFlow = observeChartConfigUseCase()
    private val canvasSize = MutableStateFlow(IntSize.Zero)
    private val tapPosition = MutableStateFlow<Offset?>(null)
    private val _selectedIndex = MutableStateFlow<Int?>(null)
    private val _processedChartData = MutableStateFlow<List<Chart>>(emptyList())


    init {
        //TODO костыль инициализации signalUserSettingsRepositoryImpl - избежать в будующем
        // Инициализируем настройки сигналов при создании ViewModel
        viewModelScope.launch {
            signalUserSettingsRepositoryImpl.initDefaults()
        }
    }

    private fun updateCanvas(size: IntSize) {
        if (canvasSize.value != size) {
            canvasSize.value = size
        }
    }


    private fun handleTap(position: Offset) {
        val wasSelected = tapPosition.value != null
        tapPosition.value = if (wasSelected) null else position

        if (wasSelected) {
            _selectedIndex.value = null
            return
        }

        val chartData = _processedChartData.value
        var closestIndex: Int? = null
        var minDistance = Float.MAX_VALUE

        chartData.forEach { series ->
            if (series.points.isEmpty()) return@forEach

            val idx = findClosestIndexSorted(series.points, position.x)
            val dist = (series.points[idx] - position).getDistance()

            if (dist < minDistance) {
                minDistance = dist
                closestIndex = idx
            }
        }

        _selectedIndex.value = closestIndex
    }

    private fun findClosestIndexSorted(points: List<Offset>, target: Float): Int {
        var left = 0
        var right = points.lastIndex

        while (left < right) {
            val mid = (left + right) / 2
            if (points[mid].x < target) {
                left = mid + 1
            } else {
                right = mid
            }
        }

        // Проверка ближайшего из двух соседей
        val nearest = when {
            left == 0 -> 0
            left >= points.lastIndex -> points.lastIndex
            else -> {
                val prev = points[left - 1]
                val curr = points[left]
                if ((curr.x - target) < (target - prev.x)) left else left - 1
            }
        }

        return nearest
    }

    private val versionFlow = observeElevatorVersionUseCase()
    private val currentTimeRange = getTimeRangeUseCase(versionFlow, false)
        .onEach { value ->
            Log.d("test", "This is ${value?.timeStart} ${value?.timeEnd}")
        }

    private val visibleListLine = observeListLineUseCase(
        versionFlow = versionFlow,
        rangeFlow = currentTimeRange,
        maxGapDistance = 602_000L
    )

    // todo ========================================================================================
    private val _state: StateFlow<ParametersState> = combine(
        listOf(
            visibleListLine,
            observePopDataUseCase(visibleListLine, _selectedIndex).toUIParameterDisplayDataFlow(),
            chartConfigFlow,
            canvasSize,
            currentTimeRange,
            signalUserSettingsRepositoryImpl.observe()
        )
    ) { values ->
        val domainSeries = values[0] as List<Line> // или ваш тип
        val popData = values[1] as ParameterDisplayData // или ваш тип
        val config = values[2] as ChartConfig
        val canvas = values[3] as IntSize
        val time = values[4] as TimeRange?
        val test = values[5] as List<SignalUserSettings> // или ваш тип

        val baseChartData = domainSeries.mapListLineToUiChartList(
            canvas = canvas,
            stepCounterXAxis = config.stepCount,
            test
        )

        _processedChartData.value = baseChartData

        ParametersState(
            chartData = baseChartData,
            tapPosition = tapPosition.value,
            popData = popData,
            chartConfig = config,
            timeRange = time
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ParametersStateDefaults.getDefault()
    )

    val state: StateFlow<ParametersState> = _state

    private fun handleChartEvent(event: ParametersEvents) {
        viewModelScope.launch {
            when (event) {
                is ParametersEvents.EditTimeRange -> updateTimeRangeUseCase(
                    event.newTimeRange,
                    versionFlow = versionFlow,
                )

                is ParametersEvents.ChangeScale -> updateChartScaleUseCase(event.scale)
                is ParametersEvents.ChangeOffset -> updateChartOffsetUseCase(event.offset)
                is ParametersEvents.ChangeCanvasSize -> updateCanvas(event.size)
                is ParametersEvents.Tap -> handleTap(event.touchPosition)

            }
        }
    }

    val onEvents: (ParametersEvents) -> Unit = ::handleChartEvent
}


