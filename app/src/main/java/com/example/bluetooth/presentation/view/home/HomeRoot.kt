package com.example.bluetooth.presentation.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.R
import com.example.bluetooth.presentation.view.home.components.ControlButtons
import com.example.bluetooth.presentation.view.home.components.TerminalDataBox
import com.example.bluetooth.presentation.view.home.state.HomeState
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.domain.model.ButtonType
import com.example.domain.model.ControllerConfig
import com.example.domain.model.KeyMode
import com.example.domain.model.Range
import com.example.domain.model.Rotate


@Composable
fun HomeRoot(
    viewModel: DeviceExchangeViewModel = viewModel(),
) {
    Home(
        state = HomeState(
            data = viewModel.data.collectAsState().value,
            controllerConfig = viewModel.controllerConfig.collectAsState().value,
            onEvents = remember { { event -> viewModel.onEvents(event) } },
            test = viewModel.test.collectAsState().value,
        ),
    )
}

//@NonRestartableComposable
@Composable
private fun Home(state: HomeState) {
    val buttons =
        remember(state.controllerConfig.keyMode) { getButtonsForKeyMode(state.controllerConfig.keyMode) }
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Blue),
                contentAlignment = Alignment.Center
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
            if (state.controllerConfig.keyMode != KeyMode.NONE) {
                ControlButtons(
                    onEvents = state.onEvents,
                    buttons = buttons,
                    modifier = Modifier
                        .wrapContentSize()
                        .fillMaxWidth()
                )
            }
        }
    }
}

data class ControlButtonData(val labelRes: Int, val buttonType: ButtonType)

private val basicButtons by lazy {
    listOf(
        ControlButtonData(R.string.button_help_box_button_label_close, ButtonType.Close),
        ControlButtonData(R.string.button_help_box_button_label_open, ButtonType.Open),
        ControlButtonData(R.string.button_help_box_button_label_stop, ButtonType.Stop),
        ControlButtonData(R.string.button_help_box_button_label_burner, ButtonType.Burner),
        ControlButtonData(R.string.button_help_box_button_label_f, ButtonType.F),
        ControlButtonData(R.string.button_help_box_button_label_cancel, ButtonType.Cancel),
        ControlButtonData(R.string.button_help_box_button_label_enter, ButtonType.Enter),
        ControlButtonData(R.string.button_help_box_button_label_up_arrow, ButtonType.ArrowUp),
        ControlButtonData(R.string.button_help_box_button_label_down_arrow, ButtonType.ArrowDown)
    )
}

private val advancedButtons by lazy {
    listOf(
        ControlButtonData(R.string.button_help_box_button_label_one, ButtonType.One),
        ControlButtonData(R.string.button_help_box_button_label_two, ButtonType.Two),
        ControlButtonData(R.string.button_help_box_button_label_three, ButtonType.Three),
        ControlButtonData(R.string.button_help_box_button_label_four, ButtonType.Four),
        ControlButtonData(R.string.button_help_box_button_label_five, ButtonType.Five),
        ControlButtonData(R.string.button_help_box_button_label_six, ButtonType.Six),
        ControlButtonData(R.string.button_help_box_button_label_seven, ButtonType.Seven),
        ControlButtonData(R.string.button_help_box_button_label_eight, ButtonType.Eight),
        ControlButtonData(R.string.button_help_box_button_label_nine, ButtonType.Nine),
        ControlButtonData(R.string.button_help_box_button_label_zero, ButtonType.Zero),
        ControlButtonData(R.string.button_help_box_button_label_minus, ButtonType.Minus),
        ControlButtonData(R.string.button_help_box_button_label_point, ButtonType.Point),
        ControlButtonData(R.string.button_help_box_button_label_delete, ButtonType.ArrowUp),
        ControlButtonData(R.string.button_help_box_button_label_cancel, ButtonType.Cancel),
        ControlButtonData(R.string.button_help_box_button_label_enter, ButtonType.Enter)
    )
}

private fun getButtonsForKeyMode(keyMode: KeyMode): List<ControlButtonData> {
    return when (keyMode) {
        KeyMode.BASIC -> basicButtons
        else -> advancedButtons
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