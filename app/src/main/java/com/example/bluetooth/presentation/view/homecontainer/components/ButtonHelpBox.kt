package com.example.bluetooth.presentation.view.homecontainer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun ButtonHelpBox(onButtonClick: (Int) -> Unit) {
    val buttonLabels = listOf("Меню", "Режим", "Ввод", "Отмена", "Архив", "F", "↑", "↓")

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(buttonLabels) { label ->
            Button(
                onClick = { onButtonClick(buttonLabels.indexOf(label)) },
                modifier = Modifier
                    .padding(0.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(18.dp))
                    .background(Color.White, RoundedCornerShape(18.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = label,
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
        ButtonHelpBox(onButtonClick = {})
    }
}