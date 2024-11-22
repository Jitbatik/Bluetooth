package com.example.bluetooth.presentation.view.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.home.CharUI
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.bluetooth.ui.theme.psisFontFamily
import com.example.domain.model.ControllerConfig
import com.example.domain.model.KeyMode
import com.example.domain.model.Range
import com.example.domain.model.Rotate


//TODO: проверить рекомпозиции
//TODO: SelectionCanvas__
@NonRestartableComposable
@Composable
fun TerminalDataBox(
    charUIList: List<CharUI>,
    isBorder: Boolean,
    range: Range,
    lines: Int,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val charPerLine = (charUIList.size + lines - 1) / lines
    var cellSize by remember { mutableStateOf(Pair(0f, 0f)) }
    var gridOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    Box(
        modifier = modifier,
    ) {
        CharGrid(
            charUIList = charUIList,
            columns = charPerLine,
            rows = lines,
            onEvent = onEvent,
            onCellSizeChanged = { cellWidth, cellHeight, offset ->
                if (cellSize != Pair(cellWidth, cellHeight)) {
                    cellSize = Pair(cellWidth, cellHeight)
                    gridOffset = offset
                }
            },
        )
        if (isBorder) {
            SelectionCanvas__(
                cellSize = cellSize,
                gridOffset = gridOffset,
                range = range,
            )
        }
    }
}


@NonRestartableComposable
@Composable
private fun CharGrid(
    charUIList: List<CharUI>,
    columns: Int,
    rows: Int,
    onEvent: (HomeEvent) -> Unit,
    onCellSizeChanged: (cellWidth: Float, cellHeight: Float, offset: Offset) -> Unit,
) {
    val rowsContent = charUIList.chunked(columns).take(rows)
    val annotatedString = buildAnnotatedString {
        rowsContent.forEachIndexed { rowIndex, rowItems ->
            rowItems.forEach { charUI ->
                withStyle(
                    style = SpanStyle(
                        color = charUI.color,
                        background = charUI.background
                    )
                ) {
                    append(charUI.char)
                }
            }
            if (rowIndex < rowsContent.size - 1) {
                append("\n")
            }
        }
    }

    var cellWidth by remember { mutableFloatStateOf(0f) }
    var cellHeight by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = annotatedString,
            fontFamily = psisFontFamily,
            fontSize = 16.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
            maxLines = rows,
            modifier = Modifier
                .onGloballyPositioned { layoutCoordinates ->
                    val gridSize = layoutCoordinates.size
                    if (gridSize != IntSize.Zero) {
                        val newWidth = gridSize.width / columns.toFloat()
                        val newHeight = gridSize.height / rows.toFloat()

                        val offset = layoutCoordinates.positionInParent()
                        if (cellWidth != newWidth || cellHeight != newHeight) {
                            cellWidth = newWidth
                            cellHeight = newHeight
                            onCellSizeChanged(cellWidth, cellHeight, offset)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val col = ((offset.x) / cellWidth).toInt() + 1
                            val row = ((offset.y) / cellHeight).toInt() + 1
                            onEvent(HomeEvent.Press(col, row))
                            tryAwaitRelease()
                            onEvent(HomeEvent.Press(0, 0))
                        }
                    )
                },
            //TODO: допилить авторазмер текста
        )
    }
}

@Composable
fun SelectionCanvas__(
    cellSize: Pair<Float, Float>,
    gridOffset: Offset,
    range: Range,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    val topLeft = remember(range, cellSize, gridOffset) {
        Offset(
            gridOffset.x + (range.startCol - 1) * cellSize.first,
            gridOffset.y + (range.startRow - 1) * cellSize.second
        )
    }
    val bottomRight = remember(range, cellSize, gridOffset) {
        Offset(
            gridOffset.x + (range.endCol - 1) * cellSize.first,
            gridOffset.y + (range.endRow - 1) * cellSize.second
        )
    }
    val width = remember(topLeft, bottomRight) { bottomRight.x - topLeft.x }
    val height = remember(topLeft, bottomRight) { bottomRight.y - topLeft.y }

    Canvas(
        modifier = Modifier
            .size(width.dp, height.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Blue)

        val topGradient = Brush.linearGradient(
            colors = colors,
            start = Offset(topLeft.x + width * animatedValue, topLeft.y),
            end = Offset(topLeft.x + width * animatedValue - width, topLeft.y),
            tileMode = TileMode.Repeated
        )
        drawLine(
            brush = topGradient,
            start = topLeft,
            end = Offset(bottomRight.x, topLeft.y),
            strokeWidth = strokeWidth
        )

        val rightGradient = Brush.linearGradient(
            colors = colors,
            start = Offset(bottomRight.x, topLeft.y + height * animatedValue),
            end = Offset(bottomRight.x, topLeft.y + height * animatedValue - height),
            tileMode = TileMode.Repeated
        )
        drawLine(
            brush = rightGradient,
            start = Offset(bottomRight.x, topLeft.y),
            end = Offset(bottomRight.x, bottomRight.y),
            strokeWidth = strokeWidth
        )

        val bottomGradient = Brush.linearGradient(
            colors = colors,
            start = Offset(bottomRight.x - width * animatedValue, bottomRight.y),
            end = Offset(bottomRight.x - width * animatedValue + width, bottomRight.y),
            tileMode = TileMode.Repeated
        )
        drawLine(
            brush = bottomGradient,
            start = Offset(topLeft.x, bottomRight.y),
            end = bottomRight,
            strokeWidth = strokeWidth
        )

        val leftGradient = Brush.linearGradient(
            colors = colors,
            start = Offset(topLeft.x, bottomRight.y - height * animatedValue),
            end = Offset(topLeft.x, bottomRight.y - height * animatedValue + height),
            tileMode = TileMode.Repeated
        )
        drawLine(
            brush = leftGradient,
            start = topLeft,
            end = Offset(topLeft.x, bottomRight.y),
            strokeWidth = strokeWidth
        )
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
            background = getRandomColor(),
        )
    }
    val testConfig = ControllerConfig(
        range = Range(startRow = 6, endRow = 6, startCol = 1, endCol = 12),
        keyMode = KeyMode.NONE,
        rotate = Rotate.PORTRAIT,
        isBorder = false,
    )

    Surface {

        Column {
            TerminalDataBox(
                charUIList = data,
                isBorder = testConfig.isBorder,
                range = testConfig.range,
                lines = 4,
                onEvent = {}
            )
        }
    }
}
