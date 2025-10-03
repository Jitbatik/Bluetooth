package com.psis.elimlift.presentation.view.parameters.ui.chart

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke


data class ChartStyle(
    val lineStroke: Stroke = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    val verticalOffset: Float = 10f,
    val verticalOffsetFactor: Float = 0.3f,
    val maxYThresholdFactor: Float = 3f,
    val shadowAlpha: Float = 0.3f,
    val pointRadiusFactor: Float = 10f,
    val valueFormatter: (Float) -> String = { "%.1f".format(it) }
)