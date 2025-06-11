package com.example.transfer.chart.domain.model


data class ChartConfig(
    val scale: Float = 1f,
    val minScale: Float = 1f,
    val maxScale: Float = 2f,
    val offset: Float = 0f,
    val minOffsetX: Float = 0f,
    val maxOffsetX: Float = 0f,
    val minScalePoint: Int = 300,
    val maxScalePoint: Int = 5,
) {
    val stepCount: Int
        get() = (minScalePoint + ((scale - minScale) *
                (maxScalePoint - minScalePoint) / (maxScale - minScale))).toInt()
}