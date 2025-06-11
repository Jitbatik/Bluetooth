package com.example.bluetooth.presentation.view.home

import com.example.bluetooth.Event
import override.ui.ButtonType


sealed interface HomeEvent : Event {
    data class ButtonClick(val buttons: List<ButtonType>) : HomeEvent
    data class Press(val column: Int, val row: Int) : HomeEvent
}