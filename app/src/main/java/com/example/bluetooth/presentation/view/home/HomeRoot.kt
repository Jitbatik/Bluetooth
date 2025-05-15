package com.example.bluetooth.presentation.view.home

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.transfer.model.ControllerConfig
import com.example.transfer.model.KeyMode
import com.example.transfer.model.Range
import com.example.transfer.model.Rotate
import ui.screens.Home

@NonRestartableComposable
@Composable
fun HomeRoot(
    viewModel: DataExchangeViewModel = viewModel(),
) {
    val screenData by viewModel.data.collectAsState()
    val controllerConfig by viewModel.controllerConfig.collectAsState()
    val test by viewModel.test.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val onEvents: (HomeEvent) -> Unit = remember {
        { event -> viewModel.onEvents(event) }
    }

    Home(
        state = HomeState(
            data = screenData,
            controllerConfig = controllerConfig,
            onEvents = onEvents,
            test = test,
            isConnected = isConnected,
        ),
    )
}


@PreviewLightDark
@Composable
private fun HomePreview() = BluetoothTheme {
    val sentence =
        "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

    fun getRandomColor(): Color {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color(r, g, b)
    }

//    val hexInput = arrayOf(0x00, 0x32, 0x00, 0x48, 0x00, 0x48)
//    val data1 = hexInput.map { byte ->
//        CharUI(
//            char = byte.toChar(),
//        )
//    }
    val data = sentence.map { char ->
        DataUI(
            data = char.toString(),
            color = Color.Black, //getRandomColor(),
            background = getRandomColor(),
        )
    }

    val testConfig = ControllerConfig(
        range = Range(startRow = 6, endRow = 6, startCol = 1, endCol = 12),
        keyMode = KeyMode.NONE,
        rotate = Rotate.PORTRAIT,
        isBorder = false,
    )
    Surface {
        Home(
            state = HomeState(
                data = data,
                controllerConfig = testConfig,
                onEvents = {},
                test = "test",
                isConnected = false
            ),
        )
    }
}