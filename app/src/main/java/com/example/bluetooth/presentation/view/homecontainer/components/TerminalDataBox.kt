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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.homecontainer.CharUI
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.bluetooth.ui.theme.psisFontFamily


@Composable
fun TerminalDataBox(charUIList: List<CharUI>, rows: Int) {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val charsPerRow = (charUIList.size + rows - 1) / rows

    Box(
        modifier = Modifier
            .background(Color(0xFF61D7A4))
            .padding(top = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            charUIList.chunked(charsPerRow).take(rows).forEach { row ->
                val rowText = buildAnnotatedString {
                    row.forEach { charUI ->
                        val textColor = charUI.color
                        val backgroundColor = charUI.background
                        withStyle(
                            SpanStyle(
                                color = textColor,
                                background = backgroundColor,
                            )
                        ) {
                            append(charUI.char)
                        }
                    }
                }

                var textSize by remember { mutableFloatStateOf(screenWidth / (rowText.length * 0.7f)) }
                var shouldDraw by remember { mutableStateOf(false) }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .drawWithContent {
                            if (shouldDraw) {
                                drawContent()
                            }
                        },
                    text = rowText,
                    fontFamily = psisFontFamily,
                    fontSize = with(density) { textSize.toSp() },
                    lineHeight = with(density) { (textSize * 1.2f).toSp() },
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    onTextLayout = { result ->
                        if (result.didOverflowWidth) {
                            textSize *= 0.95f
                        } else {
                            shouldDraw = true
                        }
                    }
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
        CharUI(
            char = char,
            color = Color.Black, //getRandomColor(),
            background = getRandomColor()
        )
    }

    Surface {
        TerminalDataBox(data, 4)
    }
}