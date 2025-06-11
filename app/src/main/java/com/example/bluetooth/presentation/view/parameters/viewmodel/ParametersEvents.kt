package com.example.bluetooth.presentation.view.parameters.viewmodel

sealed interface ParametersEvents {
    data class ChangeOffset(val offset: Float) : ParametersEvents
    data class ChangeScale(val scale: Float) : ParametersEvents
    data class Tap(val selectedIndex: Int?) : ParametersEvents
}