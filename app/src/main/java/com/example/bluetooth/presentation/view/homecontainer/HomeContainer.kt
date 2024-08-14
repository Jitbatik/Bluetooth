package com.example.bluetooth.presentation.view.homecontainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun HomeContainer(
    //viewModel: ExchangeDataViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            ButtonFWidget(buttonType = true, onButtonClick = {})
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.Black)
            )
            TerminalDataWidget()
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.Black)
            )
            ButtonFWidget(buttonType = false, onButtonClick = {})
            ButtonHelpWidget()
        }
    }
}

@Composable
fun ButtonFWidget(buttonType: Boolean, onButtonClick: (Int) -> Unit) {
    val startIndex = if (buttonType) listOf(0, 1, 2, 3) else listOf(4, 5, 6, 7)

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        itemsIndexed(startIndex) { _, index ->
            Box(
                modifier = Modifier
                    .clip(RectangleShape)
                    .background(Color.Gray)
            ) {
                Button(
                    onClick = { onButtonClick(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                        ,
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

@Composable
fun TerminalDataWidget() {
    val messages = listOf("Message 1", "Message 2", "Message 3", "Message 4")
    Box(
        modifier = Modifier
            .background(Color(0xFF61D7A4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            messages.take(4).forEach { message ->
                Text(
                    text = message,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun ButtonHelpWidget() {

}

@PreviewLightDark
@Composable
private fun HomeContentPreview() = BluetoothTheme {
    Surface {
        HomeContainer()
    }
}