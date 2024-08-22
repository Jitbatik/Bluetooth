package com.example.bluetooth.presentation.view.homecontainer

import androidx.compose.ui.graphics.Color

data class ListData(
    val data: List<Byte>,
    val charColor: Color = Color.Black,
    val charBackground: Color = Color.Transparent
)

