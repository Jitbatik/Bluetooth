package com.example.bluetooth.presentation.view.home

sealed interface HomeEvent {
    data class ButtonClick(val pressedButton: ButtonType) : HomeEvent
    data class TextPositionTapped(val column: Int, val row: Int) : HomeEvent
}

sealed class ButtonType {
    data object Menu : ButtonType()
    data object Mode : ButtonType()
    data object Enter : ButtonType()
    data object Cancel : ButtonType()
    data object Archive : ButtonType()
    data object FButton : ButtonType()
    data class Arrow(val direction: ArrowDirection) : ButtonType()
    sealed class ArrowDirection {
        data object Up : ArrowDirection()
        data object Down : ArrowDirection()
    }

    data class F(val number: Int) : ButtonType()
}
