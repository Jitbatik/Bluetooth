package com.example.bluetooth.presentation.view.parameters.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

fun calculateTooltipOffset(
    position: Offset,
    tooltipSize: IntSize,
    parentSize: IntSize,
    margin: Float,
    pointerOffset: Float
): Offset {
    val rawX = position.x + pointerOffset
    val rawY = position.y - tooltipSize.height - pointerOffset
    val clampedX = rawX.coerceIn(margin, parentSize.width - tooltipSize.width - margin)
    val clampedY = rawY.coerceIn(margin, parentSize.height - tooltipSize.height - margin)
    return Offset(clampedX, clampedY)
}