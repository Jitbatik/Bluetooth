package com.example.bluetooth.presentation.view.homecontainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.presentation.view.homecontainer.components.ButtonFBox
import com.example.bluetooth.presentation.view.homecontainer.components.ButtonHelpBox
import com.example.bluetooth.presentation.view.homecontainer.components.TerminalDataBox

@Composable
fun HomeContainer(
    viewModel: ExchangeDataViewModel = viewModel()
) {
    val data by viewModel.data.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            ButtonFBox(buttonType = true, onButtonClick = {})
            SpacerDivider()
            if (isConnected) {
                TerminalDataBox(data, 4)
            } else {
                Text("Нет подключения")
            }
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

//
//@PreviewLightDark
//@Composable
//private fun HomeContentPreview() = BluetoothTheme {
//    Surface {
//        HomeContainer()
//    }
//}