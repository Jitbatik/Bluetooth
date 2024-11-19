package com.example.bluetooth.presentation.view.home

sealed interface HomeEvent {
    data class ButtonClick(val pressedButton: ButtonType) : HomeEvent
    data class Press(val column: Int, val row: Int) : HomeEvent
}

sealed interface ButtonType {
    data object Menu : ButtonType
    data object Mode : ButtonType
    data object Enter : ButtonType
    data object Cancel : ButtonType
    data object Archive : ButtonType
    data object FButton : ButtonType
    data class Arrow(val direction: ArrowDirection) : ButtonType
    sealed interface ArrowDirection {
        data object Up : ArrowDirection
        data object Down : ArrowDirection
    }

    data class F(val number: Int) : ButtonType

    data object Close : ButtonType
    data object Open : ButtonType
    data object Stop : ButtonType
    data object Burner : ButtonType
    data object SecondaryF : ButtonType
    data object SecondaryCancel : ButtonType
    data object SecondaryEnter : ButtonType
    data object SecondaryUp : ButtonType
    data object SecondaryDown : ButtonType

    data object One : ButtonType
    data object Two : ButtonType
    data object Three : ButtonType
    data object Four : ButtonType
    data object Five : ButtonType
    data object Six : ButtonType
    data object Seven : ButtonType
    data object Eight : ButtonType
    data object Nine : ButtonType
    data object Zero : ButtonType
    data object Minus : ButtonType
    data object Point : ButtonType
    //SecondaryUp
    //SecondaryCancel
    //SecondaryEnter
}
