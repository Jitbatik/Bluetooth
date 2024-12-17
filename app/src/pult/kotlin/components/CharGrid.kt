package components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.home.CharUI
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.ui.theme.psisFontFamily

//todo: рекомпозиция charUIList меняется
@Composable
fun CharGrid(
    charUIList: () -> List<CharUI>,
    rows: Int,
    onEvent: (HomeEvent) -> Unit,
    onCellSizeChanged: (cellWidth: Float, cellHeight: Float, offset: Offset) -> Unit,
) {
    val test = charUIList()
    val charInLine = if (test.isNotEmpty()) test.size / rows else 0
    val rowsContent = test.chunked(charInLine)
    val annotatedString = buildAnnotatedString {
        rowsContent.forEachIndexed { index, line ->
            line.forEach { charUI ->
                withStyle(
                    style = SpanStyle(
                        color = charUI.color,
                        background = charUI.background
                    )
                ) {
                    append(charUI.char)
                }
            }
            if (index < rowsContent.size - 1) {
                append("\n")
            }
        }
    }
    var fontSize by remember { mutableStateOf(16.sp) }

    var cellWidth by remember { mutableFloatStateOf(0f) }
    var cellHeight by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .onSizeChanged { size ->
                val containerWidthPx = size.width.toFloat()
                fontSize = when {
                    containerWidthPx < 100f -> 12.sp
                    containerWidthPx < 200f -> 14.sp
                    else -> 15.sp
                }
            }

            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = annotatedString,
            fontFamily = psisFontFamily,
            fontSize = fontSize,
            lineHeight = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .onGloballyPositioned { layoutCoordinates ->
                    val gridSize = layoutCoordinates.size
                    if (gridSize != IntSize.Zero) {
                        val newWidth = gridSize.width / charInLine.toFloat()
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
        )
    }
}