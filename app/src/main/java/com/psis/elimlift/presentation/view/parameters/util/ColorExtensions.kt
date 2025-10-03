package com.psis.elimlift.presentation.view.parameters.util

import androidx.compose.ui.graphics.Color
import com.psis.transfer.chart.domain.model.SignalColor


fun SignalColor.toUiColor(): Color {
    return Color(red, green, blue)
}





