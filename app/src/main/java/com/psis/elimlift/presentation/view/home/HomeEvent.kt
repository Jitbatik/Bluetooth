package com.psis.elimlift.presentation.view.home

import com.psis.elimlift.Event
import override.ui.ButtonType


sealed interface HomeEvent : Event {
    data class ButtonClick(val buttons: List<ButtonType>) : HomeEvent
    data class Press(val column: Int, val row: Int) : HomeEvent
}