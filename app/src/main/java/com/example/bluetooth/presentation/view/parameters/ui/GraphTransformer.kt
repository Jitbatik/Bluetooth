package com.example.bluetooth.presentation.view.parameters.ui

data class GraphTransformer(
    val minX: Float,
    val minY: Float,
    val stepX: Float,
    val stepY: Float,
    val height: Float,
    val verticalOffset: Float = 0f
) {
    fun toCanvasX(x: Float): Float = (x - minX) * stepX
    fun toCanvasY(y: Float): Float = height - (y - minY) * stepY - verticalOffset
}