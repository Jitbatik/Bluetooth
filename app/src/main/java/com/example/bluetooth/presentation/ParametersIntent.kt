package com.example.bluetooth.presentation

sealed interface ParametersIntent {
    data class ChangeOffset(val offset: Float) : ParametersIntent
    data class ChangeScale(val scale: Float) : ParametersIntent
}