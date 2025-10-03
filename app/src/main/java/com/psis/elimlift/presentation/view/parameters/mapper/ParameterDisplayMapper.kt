package com.psis.elimlift.presentation.view.parameters.mapper

import com.psis.elimlift.presentation.view.parameters.model.ParameterDisplayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.psis.transfer.chart.domain.model.ParameterDisplayData as DomainParameterDisplayData

fun DomainParameterDisplayData.toUIParameterDisplayData(): ParameterDisplayData =
    ParameterDisplayData(
        selectedIndex = this.selectedIndex,
        timestamp = this.timestamp,
        timeMilliseconds = this.timeMilliseconds,
        parameters = this.parameters.mapValues { it.value.toUi() }
    )

fun Flow<DomainParameterDisplayData>.toUIParameterDisplayDataFlow(): Flow<ParameterDisplayData> =
    map { it.toUIParameterDisplayData() }