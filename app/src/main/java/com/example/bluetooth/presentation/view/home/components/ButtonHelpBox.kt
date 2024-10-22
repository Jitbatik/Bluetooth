package com.example.bluetooth.presentation.view.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.R
import com.example.bluetooth.presentation.view.connect.components.AnimatedButtonDecrease
import com.example.bluetooth.presentation.view.home.ButtonType
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.bluetooth.presentation.view.home.HomeEvent

@Composable
fun ButtonHelpBox(onEvent: (HomeEvent) -> Unit) {
    val buttonLabels = listOf(
        ButtonType.Menu,
        ButtonType.Mode,
        ButtonType.Enter,
        ButtonType.Cancel,
        ButtonType.Archive,
        ButtonType.FButton,
        ButtonType.Arrow(ButtonType.ArrowDirection.Up),
        ButtonType.Arrow(ButtonType.ArrowDirection.Down),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(buttonLabels) { buttonType ->
            AnimatedButtonDecrease(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
                shape = RoundedCornerShape(18.dp),
                onClick = { onEvent(HomeEvent.ButtonClick(pressedButton = buttonType)) },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = when (buttonType) {
                        is ButtonType.Menu -> stringResource(R.string.button_help_box_button_label_menu)
                        is ButtonType.Mode -> stringResource(R.string.button_help_box_button_label_mode)
                        is ButtonType.Enter -> stringResource(R.string.button_help_box_button_label_enter)
                        is ButtonType.Cancel -> stringResource(R.string.button_help_box_button_label_cancel)
                        is ButtonType.Archive -> stringResource(R.string.button_help_box_button_label_archive)
                        is ButtonType.FButton -> stringResource(R.string.button_help_box_button_label_f)
                        is ButtonType.Arrow -> if (buttonType.direction is ButtonType.ArrowDirection.Up) {
                            stringResource(R.string.button_help_box_button_label_up_arrow)
                        } else {
                            stringResource(R.string.button_help_box_button_label_down_arrow)
                        }

                        else -> "Unknown Button"
                    },
                    fontSize = 16.sp
                )
            }
        }
    }
}


@PreviewLightDark
@Composable
private fun ButtonHelpBoxPreview() = BluetoothTheme {
    Surface {
        ButtonHelpBox(onEvent = {})
    }
}