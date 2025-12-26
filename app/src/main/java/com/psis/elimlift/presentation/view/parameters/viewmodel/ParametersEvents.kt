package com.psis.elimlift.presentation.view.parameters.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.psis.transfer.chart.domain.model.TimeRange

sealed interface ParametersEvents {
    data class ChangeOffset(val offset: Float) : ParametersEvents
    data class ChangeScale(val scale: Float) : ParametersEvents
    data class ChangeCanvasSize(val size: IntSize) : ParametersEvents

    data class Tap(val touchPosition: Offset) : ParametersEvents
    data class EditTimeRange(val oldTimeRange: TimeRange, val newTimeRange: TimeRange) :
        ParametersEvents
}