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
//    data object Close : ModbusButtonType
//    data object Open : ModbusButtonType
//    data object Stop : ModbusButtonType
//    data object Burner : ModbusButtonType
//    data object F : ModbusButtonType
//    data object Cancel : ModbusButtonType
//    data object Enter : ModbusButtonType
//    data object ArrowUp : ModbusButtonType
//    data object ArrowDown : ModbusButtonType
//
//    data object One : ModbusButtonType
//    data object Two : ModbusButtonType
//    data object Three : ModbusButtonType
//    data object Four : ModbusButtonType
//    data object Five : ModbusButtonType
//    data object Six : ModbusButtonType
//    data object Seven : ModbusButtonType
//    data object Eight : ModbusButtonType
//    data object Nine : ModbusButtonType
//    data object Zero : ModbusButtonType
//    data object Minus : ModbusButtonType
//    data object Point : ModbusButtonType
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

data class Command(
    val coordinateX: Int,
    val coordinateY: Int,
    val type: ButtonType
)



//data object Menu : ButtonType
//data object Mode : ButtonType
//data object Enter : ButtonType
//data object Cancel : ButtonType
//data object Archive : ButtonType
//data object FButton : ButtonType
//data object ArrowUp : ButtonType
//data object ArrowDown : ButtonType
//
//data class F(val number: Int) : ButtonType
//    data object Close : ButtonType
//    data object Open : ButtonType
//    data object Stop : ButtonType
//    data object Burner : ButtonType

//    data object One : ButtonType
//    data object Two : ButtonType
//    data object Three : ButtonType
//    data object Four : ButtonType
//    data object Five : ButtonType
//    data object Six : ButtonType
//    data object Seven : ButtonType
//    data object Eight : ButtonType
//    data object Nine : ButtonType
//    data object Zero : ButtonType
//    data object Minus : ButtonType
//    data object Point : ButtonType
//    data object None : ButtonType
//    fun toKey(): String = when (this) {
//        Menu -> "Menu"
//        Mode -> "Mode"
//        Enter -> "Enter"
//        is F -> "F-${number}"
//        Archive -> "Archive"
//        ArrowDown -> "ArrowDown"
//        ArrowUp -> "ArrowUp"
//        Burner -> "Burner"
//        Cancel -> "Cancel"
//        Close -> "Close"
//        Eight -> "Eight"
//        FButton -> "FButton"
//        Five -> "Five"
//        Four -> "Four"
//        Minus -> "Minus"
//        Nine -> "Nine"
//        None -> "None"
//        One -> "One"
//        Open -> "Open"
//        Point -> "Point"
//        Seven -> "Seven"
//        Six -> "Six"
//        Stop -> "Stop"
//        Three -> "Three"
//        Two -> "Two"
//        Zero -> "Zero"
//    }