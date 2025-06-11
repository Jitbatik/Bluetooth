package com.example.bluetooth.presentation.view.parameters.util

import androidx.compose.ui.graphics.Color
import com.example.transfer.chart.domain.model.SignalColor


fun SignalColor.toUiColor(): Color {
    return Color(red, green, blue)
}





