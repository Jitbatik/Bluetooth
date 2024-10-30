package com.example.bluetooth.presentation.view.home.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.home.CharUI
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.bluetooth.ui.theme.psisFontFamily


@Composable
fun TerminalDataBox12(
    charUIList: List<CharUI>,
    rows: Int,
    onEvent: (HomeEvent) -> Unit,
) {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val charsPerRow = (charUIList.size + rows - 1) / rows


    Box(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val x = offset.x
                    val y = offset.y

                    val textSize = screenWidth / (charsPerRow * 0.7f)

                    val col = (x / textSize).toInt() + 1
                    val row = (y / (textSize * 1.2f)).toInt() + 1
                    Log.d(
                        "test",
                        "Coordinate pressed: column $col, row $row"
                    )
                    onEvent(HomeEvent.Press(column = col, row = row))
                }
            },
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


@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun TerminalDataBox(
    charUIList: List<CharUI>,
    rows: Int,
    onEvent: (HomeEvent) -> Unit,
) {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val charsPerRow = (charUIList.size + rows - 1) / rows

    Box(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    val textSize = screenWidth / (charsPerRow * 0.7f)
                    while (true) {
                        val down = awaitPointerEvent().changes.firstOrNull { it.pressed }
                        if (down != null) {
                            var previousPosition = down.position
                            val initialCol = (previousPosition.x / textSize).toInt() + 1
                            val initialRow = (previousPosition.y / (textSize * 1.2f)).toInt() + 1
//                            val initialCol =
//                                (down.position.x / (screenWidth / charsPerRow)).toInt() + 1
//                            val initialRow =
//                                (down.position.y / (screenWidth / charsPerRow)).toInt() + 1

                            Log.d(
                                "test",
                                "Coordinate pressed $initialCol, $initialRow"
                            )
                            onEvent(HomeEvent.Press(initialCol, initialRow))

                            down.consume()
                            while (true) {
                                val event = awaitPointerEvent()
                                var isPressed = false

                                event.changes.forEach { change ->
                                    if (change.pressed) {
                                        val col =
                                            (change.position.x / textSize).toInt() + 1
                                        val row =
                                            (change.position.y / (textSize * 1.2f)).toInt() + 1

                                        if (change.position != previousPosition) {
                                            Log.d(
                                                "test",
                                                "Move to coordinate $col, $row"
                                            )
                                            onEvent(HomeEvent.Press(col, row))
                                            previousPosition = change.position
                                        }

                                        isPressed = true
                                        change.consume()
                                    } else {
                                        change.consume()
                                    }
                                }
                                if (!isPressed) {
                                    val finalCol =
                                        (previousPosition.x / textSize).toInt() + 1
                                    val finalRow =
                                        (previousPosition.y / (textSize * 1.2f)).toInt() + 1

                                    Log.d(
                                        "test",
                                        "Coordinate release $finalCol, $finalRow"
                                    )
                                    onEvent(HomeEvent.Press(0, 0))
                                    break
                                }
                            }
                        }
                    }
                }
            },
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


@Composable
fun TerminalDataBox_(
    charUIList: List<CharUI>,
    rows: Int,
    onEvent: (HomeEvent) -> Unit,
) {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val charsPerRow = (charUIList.size + rows - 1) / rows

    Box(
        modifier = Modifier
            .background(color = Color.Black)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val x = offset.x
                        val y = offset.y

                        val textSize = screenWidth / (charsPerRow * 0.7f)

                        val col = (x / textSize).toInt() + 1
                        val row = (y / (textSize * 1.2f)).toInt() + 1
                        Log.d("test", "Нажата координата:  ${col}, $row")
                        onEvent(HomeEvent.Press(col, row))
                        tryAwaitRelease()
                        Log.d("test", "Нажатие прекратилось ${col}, $row")
                        onEvent(HomeEvent.Press(0, 0))
                    }
                )
            },
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
        TerminalDataBox(
            charUIList = data,
            rows = 4,
            onEvent = {}
        )
    }
}
