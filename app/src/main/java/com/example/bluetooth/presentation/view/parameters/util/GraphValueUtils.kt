package com.example.bluetooth.presentation.view.parameters.util

import androidx.compose.ui.graphics.Color
import com.example.bluetooth.presentation.view.parameters.model.GraphSeries

fun extractLatestValues(
    graphData: List<GraphSeries>
): List<Pair<Color, Float>> {
    return graphData.mapNotNull { line ->
        line.points.lastOrNull()?.let { point -> line.color to point.y }
    }
}