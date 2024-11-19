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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.R
import com.example.bluetooth.presentation.view.home.ButtonType
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.domain.model.KeyMode

//TODO: доработать кнопку F
@Composable
fun ControlButtons(
    onEvents: (HomeEvent) -> Unit,
    keyMode: KeyMode,
    modifier: Modifier = Modifier,
) {
    if (keyMode == KeyMode.NONE) return
    val buttons = remember(keyMode) { getButtonsForKeyMode(keyMode) }
    val buttonColors = ButtonDefaults.textButtonColors(
        containerColor = Color.Gray,
        contentColor = Color.Black,
    )
    val buttonShape = RoundedCornerShape(0.dp)

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(buttons) { (labelRes, buttonType) ->
            ControlButton(
                labelRes = labelRes,
                buttonType = buttonType,
                buttonColors = buttonColors,
                buttonShape = buttonShape,
                onEvents = onEvents
            )
        }
    }
}


@Composable
private fun ControlButton(
    labelRes: Int,
    buttonType: ButtonType,
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
                        onEvents(HomeEvent.ButtonClick(pressedButton = buttonType))
                        tryAwaitRelease()
                        onEvents(HomeEvent.Press(0, 0))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(labelRes),
            color = buttonColors.contentColor,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp
            ),
            modifier = Modifier.padding(4.dp)
        )
    }
}


private fun getButtonsForKeyMode(keyMode: KeyMode): List<Pair<Int, ButtonType>> {
    return when (keyMode) {
        KeyMode.BASIC -> listOf(
            Pair(R.string.button_help_box_button_label_close, ButtonType.Close),
            Pair(R.string.button_help_box_button_label_open, ButtonType.Open),
            Pair(R.string.button_help_box_button_label_stop, ButtonType.Stop),
            Pair(R.string.button_help_box_button_label_burner, ButtonType.Burner),
            Pair(R.string.button_help_box_button_label_f, ButtonType.SecondaryF),
            Pair(R.string.button_help_box_button_label_cancel, ButtonType.SecondaryCancel),
            Pair(R.string.button_help_box_button_label_enter, ButtonType.SecondaryEnter),
            Pair(R.string.button_help_box_button_label_up_arrow, ButtonType.SecondaryUp),
            Pair(R.string.button_help_box_button_label_down_arrow, ButtonType.SecondaryDown)
        )

        KeyMode.NUMERIC -> listOf(
            Pair(R.string.button_help_box_button_label_one, ButtonType.One),
            Pair(R.string.button_help_box_button_label_two, ButtonType.Two),
            Pair(R.string.button_help_box_button_label_three, ButtonType.Three),
            Pair(R.string.button_help_box_button_label_four, ButtonType.Four),
            Pair(R.string.button_help_box_button_label_five, ButtonType.Five),
            Pair(R.string.button_help_box_button_label_six, ButtonType.Six),
            Pair(R.string.button_help_box_button_label_seven, ButtonType.Seven),
            Pair(R.string.button_help_box_button_label_eight, ButtonType.Eight),
            Pair(R.string.button_help_box_button_label_nine, ButtonType.Nine),
            Pair(R.string.button_help_box_button_label_zero, ButtonType.Zero),
            Pair(R.string.button_help_box_button_label_minus, ButtonType.Minus),
            Pair(R.string.button_help_box_button_label_point, ButtonType.Point),
            Pair(R.string.button_help_box_button_label_delete, ButtonType.SecondaryUp),
            Pair(R.string.button_help_box_button_label_cancel, ButtonType.SecondaryCancel),
            Pair(R.string.button_help_box_button_label_enter, ButtonType.SecondaryEnter)
        )

        KeyMode.NONE -> emptyList()
    }
}

