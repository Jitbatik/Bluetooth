package com.example.bluetooth.presentation.view.parameters.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color


data class Chart(
    val name: String,
    val points: List<Offset>,
    val color: Color,
    val minValue: Float,
    val maxValue: Float,
)