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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.home.ButtonState
import com.example.bluetooth.presentation.view.home.ControlButtons
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.presentation.view.home.HomeState
import components.MultiLineTextBox

@NonRestartableComposable
@Composable
fun Home(state: HomeState) {

    val buttonStates = remember { mutableStateMapOf<ButtonType, ButtonState>() }
    val isReset = remember { mutableStateOf(false) }
    val handlePressStart: (ButtonType) -> Unit = { button ->
        buttonStates[button] = ButtonState.PRESSED
        val activeButtons = buttonStates.filterValues { it != ButtonState.DEFAULT }
        if (activeButtons.count() > 1) isReset.value = !isReset.value
        state.onEvents(HomeEvent.ButtonClick(buttons = activeButtons.keys.toList()))
    }
    val handlePressEnd: (ButtonType) -> Unit = { button ->
        buttonStates[button] = ButtonState.DEFAULT
        state.onEvents(HomeEvent.Press(0, 0))
    }
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "В реализации",
                modifier = Modifier.fillMaxWidth(),
                color = Color.Magenta,
                textAlign = TextAlign.Center
            )
            MultiLineTextBox(
                charUIList = { state.data },
                lines = 4,
                modifier = Modifier
                    .background(Color(0xFF61D7A4))
                    .padding(4.dp)
                    .fillMaxWidth(),
            )
            ControlButtons(
                handlePressStart = handlePressStart,
                handlePressEnd = handlePressEnd,
                buttons = remember { mutableStateOf(ButtonLists.basic) },
                buttonStates = buttonStates,
                modifier = Modifier
                    .wrapContentSize()
                    .fillMaxWidth()
            )
        }
    }
}

object ButtonLists {
    val basic = listOf(
        ButtonType.ARROW_DOWN,
        ButtonType.ARROW_UP,
        ButtonType.CANCEL,
        ButtonType.ENTER,
    )
}