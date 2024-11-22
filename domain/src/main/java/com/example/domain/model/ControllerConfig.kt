package com.example.domain.model

data class ControllerConfig(
    val range: Range = Range(startRow = 0, endRow = 0, startCol = 0, endCol = 0),
    val rotate: Rotate = Rotate.PORTRAIT,
    val keyMode: KeyMode = KeyMode.NONE,
    val isBorder: Boolean = false,
)

data class Range(
    val startRow: Int,
    val endRow: Int,
    val startCol: Int,
    val endCol: Int,
)


enum class KeyMode {
    BASIC,
    NUMERIC,
    NONE
}

enum class Rotate {
    PORTRAIT,
    LANDSCAPE
}
