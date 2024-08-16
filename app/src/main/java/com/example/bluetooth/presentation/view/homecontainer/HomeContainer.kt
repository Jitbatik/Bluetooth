package com.example.bluetooth.presentation.view.homecontainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.homecontainer.components.ButtonFBox
import com.example.bluetooth.presentation.view.homecontainer.components.ButtonHelpBox
import com.example.bluetooth.presentation.view.homecontainer.components.TerminalDataBox
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun HomeContainer(
    //viewModel: ExchangeDataViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            ButtonFBox(buttonType = true, onButtonClick = {})
            SpacerDivider()
            val sentence =
                "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

            fun getRandomColor(): Color {
                val r = (0..255).random()
                val g = (0..255).random()
                val b = (0..255).random()
                return Color(r, g, b)
            }

            val data = sentence.map { char ->
                Pair(char, Pair(getRandomColor(), getRandomColor()))
                //Pair(char, Pair(Color.Black, Color.Gray))
            }
            TerminalDataBox(data, 4)
            SpacerDivider()
            ButtonFBox(buttonType = false, onButtonClick = {})
            ButtonHelpBox(onButtonClick = {})
        }
    }
}

@Composable
fun SpacerDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(Color.Black)
    )
}


@PreviewLightDark
@Composable
private fun HomeContentPreview() = BluetoothTheme {
    Surface {
        HomeContainer()
    }
}