package com.example.bluetooth.presentation.view.homecontainer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun ButtonFBox(buttonType: Boolean, onButtonClick: (Int) -> Unit) {
    val startIndex = if (buttonType) listOf(0, 1, 2, 3) else listOf(4, 5, 6, 7)

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(startIndex) { index ->
            Row(
                modifier = Modifier
                    .clip(RectangleShape)
                    .background(Color.Gray)
            ) {
                Button(
                    onClick = { onButtonClick(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "F${index + 1}",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButtonFBoxPreview() = BluetoothTheme {
    Surface {
        Column {
            ButtonFBox(buttonType = true, onButtonClick = {})
            ButtonFBox(buttonType = false, onButtonClick = {})
        }
    }
}