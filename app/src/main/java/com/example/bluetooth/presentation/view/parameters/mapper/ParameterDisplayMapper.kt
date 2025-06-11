package com.example.bluetooth.presentation.view.parameters.mapper

import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.transfer.chart.domain.model.ParameterDisplayData as DomainParameterDisplayData

fun DomainParameterDisplayData.toUi(): ParameterDisplayData =
    ParameterDisplayData(
        selectedIndex = this.selectedIndex,
        timestamp = this.timestamp,
        timeMilliseconds = this.timeMilliseconds,
        parameters = this.parameters.mapValues { it.value.toUi() }
    )

fun Flow<DomainParameterDisplayData>.toUIParameterDisplayData(): Flow<ParameterDisplayData> =
    map { it.toUi() }