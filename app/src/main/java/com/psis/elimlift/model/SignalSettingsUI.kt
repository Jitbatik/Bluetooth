package com.psis.elimlift.model

import androidx.compose.ui.graphics.Color

data class SignalSettingsUI(
    val id: String,
    val name: String,
    val isVisible: Boolean,
    val color: Color
)

data class ChartSettingsUI(
    val title: String,
    val description: String,
    val signals: List<SignalSettingsUI>
) 