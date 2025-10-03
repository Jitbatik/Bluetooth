package com.psis.transfer.chart.domain.model


data class GraphSeries(
    val name: String,
    private val _points: MutableList<DataPoint>, // внутреннее мутабельное хранилище
    val color: SignalColor? = null
) {
    val points: List<DataPoint> get() = _points.toList() // наружу только read-only view

    fun addPoint(point: DataPoint) {
        _points.add(point)
    }

    fun copyWithPoints(newPoints: List<DataPoint>): GraphSeries {
        return GraphSeries(name, newPoints.toMutableList(), color)
    }
}
