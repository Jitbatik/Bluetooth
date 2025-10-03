package com.psis.elimlift.presentation.view.parameters.util

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

    val maxX = (parentSize.width - tooltipSize.width - margin).coerceAtLeast(margin)
    val maxY = (parentSize.height - tooltipSize.height - margin).coerceAtLeast(margin)

    val clampedX = rawX.coerceIn(margin, maxX)
    val clampedY = rawY.coerceIn(margin, maxY)

    return Offset(clampedX, clampedY)
}
