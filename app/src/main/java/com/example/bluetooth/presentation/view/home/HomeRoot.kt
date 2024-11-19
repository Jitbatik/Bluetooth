package com.example.bluetooth.presentation.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.presentation.view.home.components.ControlButtons
import com.example.bluetooth.presentation.view.home.components.TerminalDataBox
import com.example.bluetooth.presentation.view.home.state.HomeState
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.domain.model.ControllerConfig
import com.example.domain.model.KeyMode
import com.example.domain.model.Range
import com.example.domain.model.Rotate


@Composable
fun HomeRoot(
    viewModel: DeviceExchangeViewModel = viewModel(),
) {
    val data by viewModel.data.collectAsState()
    val controllerConfig by viewModel.controllerConfig.collectAsState()
    val test by viewModel.test.collectAsState()
    val onEvents: (HomeEvent) -> Unit = remember {
        { event -> viewModel.onEvents(event) }
    }

    Home(
        state = HomeState(
            data = data,
            controllerConfig = controllerConfig,
            onEvents = onEvents,
            test = test,
        ),
    )
}


@NonRestartableComposable
@Composable
private fun Home(state: HomeState) {
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Blue),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = state.test,
                    color = Color.White
                )
            }
            TerminalDataBox(
                charUIList = state.data,
                isBorder = state.controllerConfig.isBorder,
                range = state.controllerConfig.range,
                lines = 28,
                onEvent = state.onEvents,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color = Color.Black),
            )
            ControlButtons(
                onEvents = state.onEvents,
                keyMode = state.controllerConfig.keyMode,
                modifier = Modifier
                    .wrapContentSize()
                    .fillMaxWidth()
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HomePreview() = BluetoothTheme {
    val sentence =
        "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен* ор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен  Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен  Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен  Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен  Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен  Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен  Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

    fun getRandomColor(): Color {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color(r, g, b)
    }

    val data = sentence.map { char ->
        CharUI(
            char = char,
            color = Color.Black, //getRandomColor(),
            background = getRandomColor(),
        )
    }
    val testConfig = ControllerConfig(
        range = Range(startRow = 6, endRow = 6, startCol = 1, endCol = 12),
        keyMode = KeyMode.NUMERIC,
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