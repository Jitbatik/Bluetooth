package com.example.bluetooth.presentation.view.homecontainer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.R
import com.example.bluetooth.ui.theme.BluetoothTheme

val psisFontFamily = FontFamily(
    Font(R.font.psis, FontWeight.Black)
)


@Composable
fun TerminalDataBox() {
    val sentence = "This is an example sentence for displaying characters in a grid layout."
    val chars =  sentence.take(80).toList()

    Box(
        modifier = Modifier
            .background(Color(0xFF61D7A4))
            .padding(4.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(20),
            modifier = Modifier.padding(0.dp)
        ) {
            items(chars) { char ->
                Box(
                    modifier = Modifier
                        .padding(0.dp)
                        .background(Color.Transparent)
                ) {
                    Text(
                        text = char.toString(),
                        style = TextStyle(
                            //fontFamily = psisFontFamily,
                            fontSize = 16.sp,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TerminalDataBoxPreview() = BluetoothTheme {
    Surface {
        TerminalDataBox()
    }
}