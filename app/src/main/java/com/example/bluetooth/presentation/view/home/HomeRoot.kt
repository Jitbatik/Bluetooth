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
import com.example.transfer.protocol.domain.model.ControllerConfig
import com.example.transfer.protocol.domain.model.KeyMode
import com.example.transfer.protocol.domain.model.Range
import com.example.transfer.protocol.domain.model.Rotate
import override.ui.Home

@NonRestartableComposable
@Composable
fun HomeRoot(
    viewModel: DataExchangeViewModel = viewModel(),
) {
    val screenData by viewModel.data.collectAsState()
    val onEvents: (HomeEvent) -> Unit = remember {
        { event -> viewModel.onEvents(event) }
    }

    Home(
        state = HomeState(
            data = screenData,
            onEvents = onEvents,
        ),
    )
}


@PreviewLightDark
@Composable
private fun HomePreview() = BluetoothTheme {
    val sentence =
          "                    Соединение прерванноПроверьте Bluetooth                     "
    fun getRandomColor(): Color {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color(r, g, b)
    }

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
            ),
        )
    }
}