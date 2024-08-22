package com.example.bluetooth.presentation.view.homecontainer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.presentation.view.homecontainer.components.ButtonFBox
import com.example.bluetooth.presentation.view.homecontainer.components.ButtonHelpBox
import com.example.bluetooth.presentation.view.homecontainer.components.TerminalDataBox
import com.example.bluetooth.ui.theme.BluetoothTheme

private const val HOME_CONTAINER = "HOME_CONTAINER"

@Composable
fun HomeContainer(
    viewModel: ExchangeDataViewModel = viewModel()
) {
    val data by viewModel.data.collectAsState()

    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            ButtonFBox(buttonType = true, onButtonClick = {
                Log.d(HOME_CONTAINER, "Index button $it")
            })
            SpacerDivider()
            TerminalDataBox(data, 4)
            SpacerDivider()
            ButtonFBox(buttonType = false, onButtonClick = {})
            ButtonHelpBox(onButtonClick = {})
            Button(onClick = { viewModel.requestPacketData() }) {
                Text(text = "test")
            }
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