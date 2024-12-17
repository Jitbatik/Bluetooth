package components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.bluetooth.presentation.view.home.CharUI
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.transfer.model.ControllerConfig
import com.example.transfer.model.KeyMode
import com.example.transfer.model.Range
import com.example.transfer.model.Rotate

@NonRestartableComposable
@Composable
fun TerminalDataBox(
    charUIList: () -> List<CharUI>,
    isBorder: Boolean,
    range: Range,
    lines: Int,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var cellSize by remember { mutableStateOf(Pair(0f, 0f)) }
    var gridOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    Box(
        modifier = modifier,
    ) {
        CharGrid(
            charUIList = charUIList,
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
            SelectionCanvas(
                cellSize = cellSize,
                gridOffset = gridOffset,
                range = range,
            )
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
            color = Color.Black,
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
                charUIList = { data },
                isBorder = testConfig.isBorder,
                range = testConfig.range,
                lines = 4,
                onEvent = {}
            )
        }
    }
}