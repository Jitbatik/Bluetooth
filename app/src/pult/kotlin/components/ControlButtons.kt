package com.example.bluetooth.presentation.view.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.home.HomeEvent
import ButtonState
import ButtonType


//TODO: думаю можно еще отрефакторить
// и вынести buttonStates handlePress* и toggleFState(toggleButtonState)
@Composable
fun ControlButtons(
    onEvents: (HomeEvent) -> Unit,
    buttons: MutableState<List<ButtonType>>,
    modifier: Modifier = Modifier,
    buttonColors: ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = Color.Gray,
        contentColor = Color.Black,
    ),
    buttonShape: Shape = RoundedCornerShape(0.dp)
) {
    val buttonStates = remember { mutableStateMapOf<ButtonType, ButtonState>() }
    val toggleFState = remember { mutableStateOf(false) }
    val isReset = remember { mutableStateOf(false) }
    val handlePressStart: (ButtonType) -> Unit = { button ->
        if (button == ButtonType.F) toggleFState.value = !toggleFState.value
        buttonStates[button] = ButtonState.PRESSED

        val activeButtons = buttonStates.filterValues { it != ButtonState.DEFAULT }
        if (activeButtons.count() > 1) isReset.value = !isReset.value

        onEvents(HomeEvent.ButtonClick(buttons = activeButtons.keys.toList()))
    }

    val handlePressEnd: (ButtonType) -> Unit = { button ->
        buttonStates[button] = when {
            button == ButtonType.F -> if (toggleFState.value) ButtonState.ACTIVE else ButtonState.DEFAULT
            else -> ButtonState.DEFAULT
        }

        if (button != ButtonType.F || !toggleFState.value) onEvents(HomeEvent.Press(0, 0))
        if (isReset.value) {
            isReset.value = !isReset.value
            toggleFState.value = !toggleFState.value
            buttonStates.keys.forEach { key -> buttonStates[key] = ButtonState.DEFAULT }
            onEvents(HomeEvent.Press(0, 0))
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(
            items = buttons.value,
            key = { it.name }
        ) { button ->
            ControlButtonItem(
                button = button,
                isButton = buttonStates[button] ?: ButtonState.DEFAULT,
                onPressStart = { handlePressStart(button) },
                onPressEnd = { handlePressEnd(button) },
                buttonColors = buttonColors,
                buttonShape = buttonShape
            )
        }
    }
}


@Composable
private fun ControlButtonItem(
    button: ButtonType,
    isButton: ButtonState,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    buttonColors: ButtonColors,
    buttonShape: Shape
) {
    ControlButton(
        text = stringResource(button.labelRes),
        onPressStart = onPressStart,
        onPressEnd = onPressEnd,
        modifier = Modifier
            .fillMaxWidth(0.25f)
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = buttonColors.containerColor,
                shape = buttonShape
            )
            .background(
                color = when (isButton) {
                    ButtonState.ACTIVE -> Color(0xff0f8c8a)
                    ButtonState.PRESSED -> Color.DarkGray
                    ButtonState.DEFAULT -> buttonColors.containerColor
                },
                shape = buttonShape
            ),
        color = buttonColors.contentColor
    )
}

@Composable
private fun ControlButton(
    text: String,
    onPressStart: () -> Unit,
    modifier: Modifier = Modifier,
    onPressEnd: () -> Unit = {},
    color: Color = Color.Unspecified,
    contentAlignment: Alignment = Alignment.Center,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 18.sp,
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        tryAwaitRelease()
                        onPressEnd()
                    },
                )
            },
        contentAlignment = contentAlignment,
    ) {
        Text(
            text = text,
            color = color,
            style = TextStyle(
                fontSize = fontSize,
                lineHeight = lineHeight,
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}
