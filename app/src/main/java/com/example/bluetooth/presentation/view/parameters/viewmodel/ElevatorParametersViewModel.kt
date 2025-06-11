package com.example.bluetooth.presentation.view.parameters.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.bluetooth.presentation.view.parameters.mapper.toUIChartDataList
import com.example.bluetooth.presentation.view.parameters.mapper.toUIParameterDisplayData
import com.example.transfer.chart.domain.usecase.GetVisibleChartDataUseCase
import com.example.transfer.chart.domain.model.ChartConfig
import com.example.transfer.protocol.domain.model.Type
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import override.navigation.NavigationItem
import javax.inject.Inject

@HiltViewModel
class ElevatorParametersViewModel @Inject constructor(
    navigationStateHolder: NavigationStateHolder,
    private val getVisibleChartDataUseCase: GetVisibleChartDataUseCase
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


    private val _state: StateFlow<ParametersState> = combine(
        getVisibleChartDataUseCase.observeTime(),
        getVisibleChartDataUseCase.observeChartData(observationType).toUIChartDataList(),
        getVisibleChartDataUseCase.parameterDisplayData.toUIParameterDisplayData(),
        getVisibleChartDataUseCase.chartConfig
    ) { time, chartData, popData, config ->
        ParametersState(
            time = time,
            chartData = chartData,
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
        val currentConfig = getVisibleChartDataUseCase.chartConfig.value
        var newConfig: ChartConfig = currentConfig
        when (event) {
            is ParametersEvents.ChangeScale -> newConfig = currentConfig.copy(scale = event.scale)
            is ParametersEvents.ChangeOffset -> newConfig =
                currentConfig.copy(offset = event.offset)

            is ParametersEvents.Tap -> getVisibleChartDataUseCase.updateSelectedIndex(event.selectedIndex)
        }
        getVisibleChartDataUseCase.updateConfig(newConfig)
    }

    val onEvents: (ParametersEvents) -> Unit = ::handleChartEvent
}



