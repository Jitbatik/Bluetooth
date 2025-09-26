package com.example.bluetooth.presentation.view.parameters.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.view.parameters.mapper.filterVisibleRange
import com.example.bluetooth.presentation.view.parameters.mapper.mapToUiChartList
import com.example.bluetooth.presentation.view.parameters.mapper.toUIParameterDisplayDataFlow
import com.example.bluetooth.presentation.view.parameters.model.Chart
import com.example.transfer.chart.domain.usecase.ChartRangeObserver
import com.example.transfer.chart.domain.usecase.ObserveChartConfigUseCase
import com.example.transfer.chart.domain.usecase.ObserveChartDataUseCase
import com.example.transfer.chart.domain.usecase.ObserveChartSettings
import com.example.transfer.chart.domain.usecase.ObservePopDataUseCase
import com.example.transfer.chart.domain.usecase.ObserveTimeUseCase
import com.example.transfer.chart.domain.usecase.UpdateChartOffsetUseCase
import com.example.transfer.chart.domain.usecase.UpdateChartScaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElevatorParametersViewModel @Inject constructor(
    observeChartDataUseCase: ObserveChartDataUseCase,
    observePopDataUseCase: ObservePopDataUseCase,
    observeTimeUseCase: ObserveTimeUseCase,
    observeChartConfigUseCase: ObserveChartConfigUseCase,

    private val updateChartScaleUseCase: UpdateChartScaleUseCase,
    private val updateChartOffsetUseCase: UpdateChartOffsetUseCase,
    chartRangeObserver: ChartRangeObserver,
    observeChartSettings: ObserveChartSettings,
) : ViewModel() {
    // Пока оставлю так если будут идеи как это убрать уберу
    // todo ========================================================================================
    private val chartConfigFlow = observeChartConfigUseCase()
    private val chartDataFlow = observeChartDataUseCase()
    private val canvasSize = MutableStateFlow(IntSize.Zero)
    private val tapPosition = MutableStateFlow<Offset?>(null)
    private val _selectedIndex = MutableStateFlow<Int?>(null)
    private val _processedChartData = MutableStateFlow<List<Chart>>(emptyList())

    // Только видимая часть графиков — используется в UI
    private val visibleChartDataFlow = combine(
        chartDataFlow,        // Flow<List<GraphSeries>>
        chartConfigFlow,      // Flow<ChartConfig>
        observeChartSettings(viewModelScope) // Flow<ChartSettings>
    ) { data, config, settings ->

        val visibleSignalNames = settings.config.signals
            .filter { it.isVisible }
            .mapTo(hashSetOf()) { it.name }

        data
            .filter { it.name in visibleSignalNames } // ⬅ фильтрация по isVisible
            .filterVisibleRange(config.offset, config.stepCount)
    }

    init {
        chartRangeObserver.start(
            scope = viewModelScope,
            chartDataFlow = chartDataFlow,
            chartConfigFlow = chartConfigFlow
        )
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

    // todo ========================================================================================
    private val _state: StateFlow<ParametersState> = combine(
        observeTimeUseCase(),
        visibleChartDataFlow,
        observePopDataUseCase(visibleChartDataFlow, _selectedIndex).toUIParameterDisplayDataFlow(),
        chartConfigFlow,
        canvasSize,
    ) { time, domainSeries, popData, config, canvas ->
        val baseChartData = domainSeries.mapToUiChartList(
            canvas = canvas,
            stepCounterXAxis = config.stepCount,
        )

        _processedChartData.value = baseChartData
        ParametersState(
            time = time,
            chartData = baseChartData,
            tapPosition = tapPosition.value,
            popData = popData,
            chartConfig = config,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ParametersStateDefaults.getDefault()
        )

    val state: StateFlow<ParametersState> = _state

    private fun handleChartEvent(event: ParametersEvents) {
        viewModelScope.launch {
            when (event) {
                is ParametersEvents.ChangeScale -> updateChartScaleUseCase(event.scale)
                is ParametersEvents.ChangeOffset -> updateChartOffsetUseCase(event.offset)
                is ParametersEvents.ChangeCanvasSize -> updateCanvas(event.size)
                is ParametersEvents.Tap -> handleTap(event.touchPosition)
            }
        }
    }

    val onEvents: (ParametersEvents) -> Unit = ::handleChartEvent
}


