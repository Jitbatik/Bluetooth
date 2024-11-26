package com.example.domain.model

sealed interface ButtonType {
    data object None : ButtonType

    data object Close : ButtonType
    data object Open : ButtonType
    data object Stop : ButtonType
    data object Burner : ButtonType
    data object F : ButtonType
    data object Cancel : ButtonType
    data object Enter : ButtonType
    data object ArrowUp : ButtonType
    data object ArrowDown : ButtonType

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

        fun toKey(): String = when (this) {
        Enter -> "Enter"
        F -> "F"
        ArrowDown -> "ArrowDown"
        ArrowUp -> "ArrowUp"
        Burner -> "Burner"
        Cancel -> "Cancel"
        Close -> "Close"
        Eight -> "Eight"
        Five -> "Five"
        Four -> "Four"
        Minus -> "Minus"
        Nine -> "Nine"
        None -> "None"
        One -> "One"
        Open -> "Open"
        Point -> "Point"
        Seven -> "Seven"
        Six -> "Six"
        Stop -> "Stop"
        Three -> "Three"
        Two -> "Two"
        Zero -> "Zero"
    }
}

//sealed interface ModbusButtonType : ButtonType {
//}
//
//sealed interface UARTButtonType : ButtonType {
//    data object Menu : UARTButtonType
//    data object Mode : UARTButtonType
//    data object Enter : UARTButtonType
//    data object Cancel : UARTButtonType
//    data object Archive : UARTButtonType
//    data object FButton : UARTButtonType
//    data object ArrowUp : UARTButtonType
//    data object ArrowDown : UARTButtonType
//
//    data class F(val number: Int) : UARTButtonType
//}