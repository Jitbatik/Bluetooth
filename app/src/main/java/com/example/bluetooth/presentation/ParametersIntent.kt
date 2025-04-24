package com.example.bluetooth.presentation

sealed interface ParametersIntent {
    data class ChangeOffset(val offset: Float) : ParametersIntent
    data class ChangeScale(val scale: Float) : ParametersIntent
    data class DataPointSelected(val timeStamp: Long, val timeMilliseconds: Int): ParametersIntent
}