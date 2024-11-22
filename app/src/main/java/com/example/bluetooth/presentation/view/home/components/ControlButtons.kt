package com.example.bluetooth.presentation.view.home.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.presentation.view.home.ControlButtonData


//TODO: доработать кнопку F
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(buttons, key = { it.buttonType.toKey() }) { button ->
            ControlButton(
                button = button,
                buttonColors = buttonColors,
                buttonShape = buttonShape,
                onEvents = onEvents
            )
        }
    }
}


@Composable
private fun ControlButton(
    button: ControlButtonData,
    buttonColors: ButtonColors,
    buttonShape: Shape,
    onEvents: (HomeEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.25f)
            .aspectRatio(1f)
            .background(Color.Gray, shape = buttonShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onEvents(HomeEvent.ButtonClick(pressedButton = button.buttonType))
                        tryAwaitRelease()
                        onEvents(HomeEvent.Press(0, 0))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(button.labelRes),
            color = buttonColors.contentColor,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}