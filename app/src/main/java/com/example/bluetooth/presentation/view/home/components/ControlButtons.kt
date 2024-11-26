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
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.bluetooth.presentation.view.home.ControlButtonData
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.presentation.view.home.state.ButtonState
import com.example.domain.model.ButtonType


//TODO: рекомпозиция
//todo: влияют onPressStart onPressEnd background
// как мне выбрать F и еще одну кнопку пока у одной кнопки статус Note если да отпрвляем их в оневент

@Composable
fun ControlButtons(
    onEvents: (HomeEvent) -> Unit,
    buttons: List<ControlButtonData>,
    modifier: Modifier = Modifier,
    buttonColors: ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = Color.Gray,
        contentColor = Color.Black,
    ),
    buttonShape: Shape = RoundedCornerShape(0.dp)
) {
    val buttonStates = remember { mutableStateMapOf<ControlButtonData, ButtonState>() }
    fun handlePressStart(button: ControlButtonData) {
        buttonStates[button] = ButtonState.Pressed
        val noteButton = buttonStates.entries.find { it.value == ButtonState.Note }?.key
        onEvents(
            HomeEvent.ButtonClick(
                pressedButton = button.buttonType,
                secondaryButton = noteButton?.buttonType,
            )
        )
    }

    fun handlePressEnd(button: ControlButtonData) {
        val activeButtonsCount = buttonStates.values.count { it != ButtonState.Idle }
        if (activeButtonsCount > 1) {
            buttonStates.keys.forEach { key -> buttonStates[key] = ButtonState.Idle }
            onEvents(HomeEvent.Press(0, 0))
        } else {
            when (button.buttonType) {
                ButtonType.F -> buttonStates[button] = ButtonState.Note
                else -> {
                    buttonStates[button] = ButtonState.Idle
                    onEvents(HomeEvent.Press(0, 0))
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(buttons, key = { it.buttonType.toKey() }) { button ->
            val currentState = buttonStates[button] ?: ButtonState.Idle
            ControlButtonItem(
                button = button,
                isButton = currentState,
                onPressStart = { handlePressStart(button) },
                onPressEnd = {  handlePressEnd(button) },
                buttonColors = buttonColors,
                buttonShape = buttonShape
            )
        }
    }
}




@Composable
private fun ControlButtonItem(
    button: ControlButtonData,
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
                    ButtonState.Note -> Color(0xff0f8c8a)
                    ButtonState.Pressed -> Color.DarkGray
                    ButtonState.Idle -> buttonColors.containerColor
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
