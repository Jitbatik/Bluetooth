package com.example.bluetooth.presentation.view.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.presentation.view.connect.components.AnimatedButtonDecrease
import com.example.bluetooth.presentation.view.home.ButtonType
import com.example.bluetooth.presentation.view.home.HomeEvent

@Composable
fun ControlButtons(
    onEvents: (HomeEvent) -> Unit,
) {
    val buttonColors = ButtonDefaults.textButtonColors(
        containerColor = Color.Gray,
        contentColor = Color.Black,
    )
    val buttonShape = RoundedCornerShape(0.dp)
    val buttons = listOf(
        Pair(R.string.button_help_box_button_label_cancel, ButtonType.SecondaryCancel),
        Pair(R.string.button_help_box_button_label_enter, ButtonType.SecondaryEnter),
        Pair(R.string.button_help_box_button_label_up_arrow, ButtonType.SecondaryUp),
        Pair(R.string.button_help_box_button_label_down_arrow, ButtonType.SecondaryDown)
    )

    Row(
        modifier = Modifier
            .background(Color.Black)
            .clip(RectangleShape),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        buttons.forEach { (labelRes, buttonType) ->
            AnimatedButtonDecrease(
                modifier = Modifier.weight(1f),
                colors = buttonColors,
                shape = buttonShape,
                contentPadding = PaddingValues(0.dp),
                onClick = { onEvents(HomeEvent.ButtonClick(pressedButton = buttonType)) }
            ) {
                Text(text = stringResource(labelRes))
            }
        }
    }
}
