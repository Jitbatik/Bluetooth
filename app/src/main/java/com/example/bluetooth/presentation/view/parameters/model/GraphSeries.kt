package com.example.bluetooth.presentation.view.parameters.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class GraphSeries(
    val name: String,
    val points: List<Offset>,
    val color: Color
)