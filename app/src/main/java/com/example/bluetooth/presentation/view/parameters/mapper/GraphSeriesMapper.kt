package com.example.bluetooth.presentation.view.parameters.mapper

import androidx.compose.ui.graphics.Color
import com.example.bluetooth.presentation.view.parameters.model.DataPoint
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries
import com.example.bluetooth.presentation.view.parameters.util.toUiColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.transfer.chart.domain.model.GraphSeries as DomainGraphSeries

fun DomainGraphSeries.toPresentation() = GraphSeries(
    name = this.name,
    points = this.points.map { point ->
        DataPoint(
            xCoordinate = point.xCoordinate,
            yCoordinate = point.yCoordinate
        )
    },
    color = this.color?.toUiColor() ?: Color.Black
)

fun Flow<List<DomainGraphSeries>>.toUIChartDataList(): Flow<List<GraphSeries>> =
    map { domainList -> domainList.map { it.toPresentation() } }


