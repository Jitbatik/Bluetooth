package com.example.bluetooth.presentation.view.parameters.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

sealed interface ParametersEvents {
    data class ChangeOffset(val offset: Float) : ParametersEvents
    data class ChangeScale(val scale: Float) : ParametersEvents
    data class ChangeCanvasSize(val size: IntSize) : ParametersEvents

    data class Tap(val touchPosition: Offset) : ParametersEvents
}