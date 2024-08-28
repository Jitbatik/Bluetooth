package com.example.bluetooth.utils

import androidx.compose.ui.graphics.Color
import com.example.bluetooth.presentation.view.homecontainer.CharUI
import com.example.domain.model.CharData

fun List<CharData>.mapToListCharUIModel(): List<CharUI> {
    return map {
        CharUI(
            char = it.charByte.toInt().toChar(),
            color = Color.Black,
            background = Color.Transparent
        )
    }
}