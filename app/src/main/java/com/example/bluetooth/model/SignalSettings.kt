package com.example.bluetooth.model

import androidx.compose.ui.graphics.Color

data class SignalSettings(
    val id: String,
    val name: String,
    val isVisible: Boolean,
    val color: Color
)

data class ChartSettings(
    val title: String,
    val description: String,
    val signals: List<SignalSettings>
) 