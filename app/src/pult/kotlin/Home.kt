import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.home.ButtonState
import com.example.bluetooth.presentation.view.home.ControlButtons
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.presentation.view.home.HomeState
import com.example.transfer.model.KeyMode
import components.TerminalDataBox

//TODO: рекомпозиции Home
@NonRestartableComposable
@Composable
fun Home(state: HomeState) {
    val buttons = remember(state.controllerConfig.keyMode) {
        mutableStateOf(getButtonsForKeyMode(state.controllerConfig.keyMode))
    }

    val buttonStates = remember { mutableStateMapOf<ButtonType, ButtonState>() }
    val toggleFState = remember { mutableStateOf(false) }
    val isReset = remember { mutableStateOf(false) }

    val handlePressStart: (ButtonType) -> Unit = { button ->
        if (button == ButtonType.F) toggleFState.value = !toggleFState.value
        buttonStates[button] = ButtonState.PRESSED

        val activeButtons = buttonStates.filterValues { it != ButtonState.DEFAULT }
        if (activeButtons.count() > 1) isReset.value = !isReset.value

        state.onEvents(HomeEvent.ButtonClick(buttons = activeButtons.keys.toList()))
    }

    val handlePressEnd: (ButtonType) -> Unit = { button ->
        buttonStates[button] = when {
            button == ButtonType.F -> if (toggleFState.value) ButtonState.ACTIVE else ButtonState.DEFAULT
            else -> ButtonState.DEFAULT
        }

        if (button != ButtonType.F || !toggleFState.value) state.onEvents(HomeEvent.Press(0, 0))
        if (isReset.value) {
            isReset.value = !isReset.value
            toggleFState.value = !toggleFState.value
            buttonStates.keys.forEach { key -> buttonStates[key] = ButtonState.DEFAULT }
            state.onEvents(HomeEvent.Press(0, 0))
        }
    }

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
                charUIList = { state.data },
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
                    buttons = buttons,
                    buttonStates = buttonStates,
                    handlePressStart = handlePressStart,
                    handlePressEnd = handlePressEnd,
                    modifier = Modifier
                        .wrapContentSize()
                        .fillMaxWidth()
                )
            }
        }
    }
}

object ButtonLists {
    val basic = listOf(
        ButtonType.CLOSE,
        ButtonType.OPEN,
        ButtonType.STOP,
        ButtonType.BURNER,
        ButtonType.F,
        ButtonType.CANCEL,
        ButtonType.ENTER,
        ButtonType.ARROW_UP,
        ButtonType.ARROW_DOWN
    )

    val advanced = listOf(
        ButtonType.ONE,
        ButtonType.TWO,
        ButtonType.THREE,
        ButtonType.FOUR,
        ButtonType.FIVE,
        ButtonType.SIX,
        ButtonType.SEVEN,
        ButtonType.EIGHT,
        ButtonType.NINE,
        ButtonType.ZERO,
        ButtonType.MINUS,
        ButtonType.POINT,
        ButtonType.CANCEL,
        ButtonType.ENTER
    )
}

private fun getButtonsForKeyMode(keyMode: KeyMode): List<ButtonType> {
    return when (keyMode) {
        KeyMode.BASIC -> ButtonLists.basic
        else -> ButtonLists.advanced
    }
}