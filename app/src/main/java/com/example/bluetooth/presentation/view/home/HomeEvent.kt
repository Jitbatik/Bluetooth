package com.example.bluetooth.presentation.view.home

import com.example.bluetooth.presentation.view.home.state.ButtonType

sealed interface HomeEvent {
    data class ButtonClick(val buttons: List<ButtonType>) : HomeEvent
    data class Press(val column: Int, val row: Int) : HomeEvent
}