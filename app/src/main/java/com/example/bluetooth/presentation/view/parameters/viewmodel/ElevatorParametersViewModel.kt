package com.example.bluetooth.presentation.view.parameters.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.bluetooth.presentation.view.parameters.mapper.maxY
import com.example.bluetooth.presentation.view.parameters.mapper.minY
import com.example.bluetooth.presentation.view.parameters.mapper.mapToUiSeriesList
import com.example.bluetooth.presentation.view.parameters.mapper.toUIParameterDisplayData
import com.example.bluetooth.presentation.view.parameters.mapper.applyJitter
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.transfer.chart.domain.usecase.ChartRangeObserver
import com.example.transfer.chart.domain.usecase.ObserveChartConfigUseCase
import com.example.transfer.chart.domain.usecase.ObserveChartDataUseCase
import com.example.transfer.chart.domain.usecase.ObserveChartSettings
import com.example.transfer.chart.domain.usecase.ObservePopDataUseCase
import com.example.transfer.chart.domain.usecase.ObserveTimeUseCase
import com.example.transfer.chart.domain.usecase.UpdateChartOffsetUseCase
import com.example.transfer.chart.domain.usecase.UpdateChartScaleUseCase
import com.example.transfer.chart.domain.usecase.filterVisibleRange
import com.example.transfer.protocol.domain.model.Type
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import override.navigation.NavigationItem
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

@HiltViewModel
class ElevatorParametersViewModel @Inject constructor(
    navigationStateHolder: NavigationStateHolder,
    private val observeChartDataUseCase: ObserveChartDataUseCase,
    observePopDataUseCase: ObservePopDataUseCase,
    observeTimeUseCase: ObserveTimeUseCase,
    observeChartConfigUseCase: ObserveChartConfigUseCase,

    private val updateChartScaleUseCase: UpdateChartScaleUseCase,
    private val updateChartOffsetUseCase: UpdateChartOffsetUseCase,
    private val chartRangeObserver: ChartRangeObserver,
    private val observeChartSettings: ObserveChartSettings,
) : ViewModel() {
    private val observationType: StateFlow<Type> = navigationStateHolder.currentScreen
        .map { screen ->
            when (screen) {
                NavigationItem.Home -> Type.READ
                NavigationItem.ParametersDashboard -> Type.READ
                else -> Type.NOTHING
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Type.NOTHING
        )

    // Пока оставлю так если будут идеи как это убрать уберу
    // todo ========================================================================================
    private val chartConfigFlow = observeChartConfigUseCase()
    private val chartDataFlow = observeChartDataUseCase(observationType, viewModelScope)
    private val canvasSize = MutableStateFlow(IntSize.Zero)
    private val tapPosition = MutableStateFlow<Offset?>(null)
    private val _selectedIndex = MutableStateFlow<Int?>(null)
    private val _processedChartData = MutableStateFlow<List<GraphSeries>>(emptyList())

    // Только видимая часть графиков — используется в UI
    private val visibleChartDataFlow = combine(
        chartDataFlow,        // Flow<List<GraphSeries>>
        chartConfigFlow,      // Flow<ChartConfig>
        observeChartSettings() // Flow<ChartSettings>
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

    private val jitterPx = 15f

    // todo ========================================================================================
    private val _state: StateFlow<ParametersState> = combine(
        observeTimeUseCase(observationType),
        visibleChartDataFlow,
        observePopDataUseCase(visibleChartDataFlow, _selectedIndex).toUIParameterDisplayData(),
        chartConfigFlow,
        canvasSize,
    ) { time, domainSeries, popData, config, canvas ->

        val maxY = domainSeries.maxY()
        val minY = domainSeries.minY()

        val yRange = max(1f, maxY - minY)
        val stepCountYAxis = ceil(yRange).toInt()
        val baseChartData = domainSeries.mapToUiSeriesList(
            canvas = canvas,
            stepCounterXAxis = config.stepCount,
            stepCountYAxis = stepCountYAxis
        )

        val uiSeriesWithJitter = baseChartData.applyJitter(jitterPx, canvas.height.toFloat())
        _processedChartData.value = uiSeriesWithJitter

        ParametersState(
            time = time,
            chartData = uiSeriesWithJitter,
            tapPosition = tapPosition.value,
            popData = popData,
            chartConfig = config,
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
                is ParametersEvents.ChangeScale -> updateChartScaleUseCase(event.scale)
                is ParametersEvents.ChangeOffset ->  updateChartOffsetUseCase(event.offset)
                is ParametersEvents.ChangeCanvasSize -> updateCanvas(event.size)
                is ParametersEvents.Tap ->  handleTap(event.touchPosition)
            }
        }
    }

    val onEvents: (ParametersEvents) -> Unit = ::handleChartEvent
}


