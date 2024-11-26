package com.example.bluetooth.presentation.view.home

import com.example.domain.model.ButtonType

sealed interface HomeEvent {
    data class ButtonClick(val pressedButton: ButtonType, val secondaryButton: ButtonType? = null) : HomeEvent
    data class Press(val column: Int, val row: Int) : HomeEvent
}