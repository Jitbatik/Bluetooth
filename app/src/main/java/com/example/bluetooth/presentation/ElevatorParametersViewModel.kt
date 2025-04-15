package com.example.bluetooth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.transfer.domain.usecase.ObserveParametersUseCase
import com.example.transfer.domain.usecase.ProcessParametersFeatureCase
import com.example.transfer.domain.usecase.Type
import com.example.transfer.model.ByteData
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.ParametersGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
    observeTypeUseCase: ObserveParametersUseCase,
    private val processParametersFeatureCase: ProcessParametersFeatureCase
) : ViewModel() {
    private val typeFlow: StateFlow<Type> = navigationStateHolder.currentScreen
        .map { screen ->
            when (screen) {
                NavigationItem.Home -> Type.READ
                NavigationItem.Parameters -> Type.READ
                else -> Type.NOTHING
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Type.NOTHING
        )

    private val dataFlow: Flow<List<ByteData>> =
        observeTypeUseCase.execute(typeFlow).map { byteDataList -> filterByteData(byteDataList) }

    private fun filterByteData(byteDataList: List<ByteData>) =
        byteDataList.take(128) + byteDataList.drop(208)

    private val _data: StateFlow<ParametersGroup> =
        processParametersFeatureCase.mapToParametersDataUI(dataFlow)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ParametersDataDefaults.getDefault()
            )

    private val _state = combine(
        _data,
        processParametersFeatureCase.chartParameters
    ) { parametersGroup, chartParameters ->
        ParametersState(
            parametersGroup = parametersGroup,
            chartParameters = chartParameters,
            onEvents = ::onEvents
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ParametersState(
            parametersGroup = ParametersDataDefaults.getDefault(),
            chartParameters = emptyMap(),
            onEvents = ::onEvents
        )
    )

    val state: StateFlow<ParametersState> = _state

    private fun onEvents(chartId: Int, event: ParametersIntent) {
        val current =
            processParametersFeatureCase.chartParameters.value[chartId] ?: ChartParameters()
        val newParams = when (event) {
            is ParametersIntent.ChangeScale -> current.copy(scale = event.scale)
            is ParametersIntent.ChangeOffset -> current.copy(offset = event.offset)
        }
        processParametersFeatureCase.updateChartParameter(chartId, newParams)
    }
}
