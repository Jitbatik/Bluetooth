package com.example.bluetooth.presentation.view.homecontainer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.bluetooth.ui.theme.psisFontFamily


@Composable
fun TerminalDataBox(charsWithStyles: List<Pair<Char, Pair<Color, Color>>>, rows: Int) {
    val charsPerRow = (charsWithStyles.size + rows - 1) / rows

    Box(
        modifier = Modifier
            .background(Color(0xFF61D7A4))
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            charsWithStyles.chunked(charsPerRow).take(rows).forEach { row ->
                val rowText = buildAnnotatedString {
                    row.forEach { (char, colors) ->
                        val (textColor, backgroundColor) = colors
                        withStyle(
                            SpanStyle(
                                color = textColor,
                                background = backgroundColor,
                            )
                        ) {
                            append(char)
                        }
                    }
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    text = rowText,
                    fontFamily = psisFontFamily,
                    fontSize = 25.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


@PreviewLightDark
@Composable
private fun TerminalDataBoxPreview() = BluetoothTheme {
    val sentence =
        "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

    fun getRandomColor(): Color {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color(r, g, b)
    }

    val data = sentence.map { char ->
        Pair(char, Pair(getRandomColor(), getRandomColor()))
    }
    Surface {
        TerminalDataBox(data, 4)
    }
}