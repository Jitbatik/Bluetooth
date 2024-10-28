package com.example.bluetooth.presentation.view.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.presentation.view.home.components.ButtonFBox
import com.example.bluetooth.presentation.view.home.components.ControlButtons
import com.example.bluetooth.presentation.view.home.components.TerminalDataBox
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun HomeRoot(
    viewModel: DeviceExchangeViewModel = viewModel(),
) {
    val data by viewModel.data.collectAsState()

    Home(
        data = data,
        onEvents = viewModel::onEvents,
    )
}

@Composable
private fun Home(
    data: List<CharUI>,
    onEvents: (HomeEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
//            ButtonFBox(
//                buttonType = true,
//                onEvent = onEvents
//            )
            TerminalDataBox(
                charUIList = data,
                rows = 28,
                onEvent = onEvents,
            )
            ControlButtons(onEvents = onEvents)
//            ButtonFBox(
//                buttonType = false,
//                onEvent = onEvents
//            )
//            ButtonHelpBox(onEvent = onEvents)
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
            background = getRandomColor()
        )
    }
    Surface {
        Home(
            data = data,
            onEvents = {}
        )
    }
}