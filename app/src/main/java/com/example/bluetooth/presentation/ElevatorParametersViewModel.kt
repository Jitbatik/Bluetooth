package com.example.bluetooth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.transfer.domain.chart.usecase.ChartParametersUseCase
import com.example.transfer.domain.parameters.Type
import com.example.transfer.model.ChartConfig
import com.example.transfer.model.LiftParameters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import navigation.NavigationItem
import javax.inject.Inject

@HiltViewModel
class ElevatorParametersViewModel @Inject constructor(
    navigationStateHolder: NavigationStateHolder,
    private val chartParametersUseCase: ChartParametersUseCase
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

    private val chartData: StateFlow<List<LiftParameters>> =
        chartParametersUseCase.observeChartData(observationType)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ParametersDataDefaults.getDefault()
            )

    private val _state = combine(
        chartData,
        chartParametersUseCase.chartConfig
    ) { parameters, config ->
        ParametersState(
            parametersGroup = parameters,
            chartConfig = config,
            onEvents = ::handleChartEvent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ParametersState(
            parametersGroup = ParametersDataDefaults.getDefault(),
            chartConfig = ChartConfig(),
            onEvents = ::handleChartEvent
        )
    )

    val state: StateFlow<ParametersState> = _state

    private fun handleChartEvent(event: ParametersIntent) {
        val currentConfig = chartParametersUseCase.chartConfig.value
        val newConfig = when (event) {
            is ParametersIntent.ChangeScale -> currentConfig.copy(scale = event.scale)
            is ParametersIntent.ChangeOffset -> currentConfig.copy(offset = event.offset)
        }
        chartParametersUseCase.updateConfig(newConfig)
    }
}
