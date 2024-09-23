package com.example.bluetooth.presentation.view.homecontainer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.bluetooth.presentation.view.connectcontainer.scanner.AnimatedButton
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.bluetooth.utils.UIEvents

@Composable
fun ButtonHelpBox(onEvent: (UIEvents) -> Unit) {
    val buttonLabels = listOf(
        stringResource(R.string.button_help_box_button_label_menu),
        stringResource(R.string.button_help_box_button_label_mode),
        stringResource(R.string.button_help_box_button_label_enter),
        stringResource(R.string.button_help_box_button_label_cancel),
        stringResource(R.string.button_help_box_button_label_archive),
        stringResource(R.string.button_help_box_button_label_f),
        stringResource(R.string.button_help_box_button_label_up_arrow),
        stringResource(R.string.button_help_box_button_label_down_arrow),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(buttonLabels) { label ->
            AnimatedButton(
                buttonColor = Color.White,
                buttonContentColor = Color.Black,
                shadowColor = Color.DarkGray,
                shadowBottomOffset = 5f,
                buttonHeight = 50f,
                shape = RoundedCornerShape(18.dp),
                onClick = {
                    val index = buttonLabels.indexOf(label)
                    val event = when (index) {
                        0 -> UIEvents.ClickButtonMenu        // Для "Меню"
                        1 -> UIEvents.ClickButtonMode        // Для "Режим"
                        2 -> UIEvents.ClickButtonInput       // Для "Ввод"
                        3 -> UIEvents.ClickButtonCancel      // Для "Отмена"
                        4 -> UIEvents.ClickButtonArchive     // Для "Архив"
                        5 -> UIEvents.ClickButtonF           // Для "F"
                        6 -> UIEvents.ClickButtonUpArrow     // Для "стрелкаВВ"
                        7 -> UIEvents.ClickButtonDownArrow   // Для "стрелкаВН"
                        else -> throw IllegalArgumentException("Invalid index")
                    }
                    onEvent(event)
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp
                )
            }
//            Button(
//                onClick = {
//                    val index = buttonLabels.indexOf(label)
//                    val event = when (index) {
//                        0 -> UIEvents.ClickButtonMenu        // Для "Меню"
//                        1 -> UIEvents.ClickButtonMode        // Для "Режим"
//                        2 -> UIEvents.ClickButtonInput       // Для "Ввод"
//                        3 -> UIEvents.ClickButtonCancel      // Для "Отмена"
//                        4 -> UIEvents.ClickButtonArchive     // Для "Архив"
//                        5 -> UIEvents.ClickButtonF           // Для "F"
//                        6 -> UIEvents.ClickButtonUpArrow     // Для "стрелкаВВ"
//                        7 -> UIEvents.ClickButtonDownArrow   // Для "стрелкаВН"
//                        else -> throw IllegalArgumentException("Invalid index")
//                    }
//                    onEvent(event)
//                },
//                modifier = Modifier
//                    .padding(0.dp)
//                    .border(1.dp, Color.Black, RoundedCornerShape(18.dp))
//                    .background(Color.White, RoundedCornerShape(18.dp)),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Transparent,
//                    contentColor = Color.Black
//                ),
//                contentPadding = PaddingValues(0.dp)
//            ) {
//                Text(
//                    text = label,
//                    fontSize = 16.sp
//                )
//            }
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