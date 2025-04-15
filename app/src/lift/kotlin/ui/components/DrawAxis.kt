package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transfer.model.AxisOrientation
import com.example.transfer.model.TickLabel


@Composable
fun DrawAxis(
    tickProvider: () -> List<TickLabel>,
    modifier: Modifier,
    axisColor: Color = Color.Magenta,
    strokeWidth: Float = 6f,
    tickHeight: Float = 20f,
    labelMargin: Dp = 8.dp,
    orientation: AxisOrientation = AxisOrientation.HORIZONTAL,
) {
    val currentTickLabels by rememberUpdatedState(tickProvider())

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val isVertical = orientation == AxisOrientation.VERTICAL

    Box(
        modifier = modifier
            .then(if (isVertical) Modifier.fillMaxHeight() else Modifier.fillMaxWidth())
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val (start, end) = if (isVertical)
                Offset(size.width, 0f) to Offset(size.width, size.height)
            else Offset(0f, size.height) to Offset(size.width, size.height)

            drawLine(
                color = axisColor,
                start = start,
                end = end,
                strokeWidth = strokeWidth
            )
            currentTickLabels.forEach { tick ->
                val tickStart = if (isVertical) Offset(size.width, tick.position)
                else Offset(tick.position, size.height)

                val tickEnd = if (isVertical) Offset(size.width - tickHeight, tick.position)
                else Offset(tick.position, size.height + tickHeight)

                drawLine(
                    color = axisColor,
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = strokeWidth
                )
            }
        }

        currentTickLabels.forEach { tick ->
            val textLayout = textMeasurer.measure(
                text = AnnotatedString(tick.label),
                style = TextStyle(fontSize = 8.sp)
            )

            val tickPosDp = with(density) { tick.position.toDp() }
            val tickOffset = with(density) { tickHeight.toDp() }
            val modifierText = if (isVertical) {
                val textHeightDp = with(density) { textLayout.size.height.toDp() }
                Modifier.offset(
                    x = tickOffset + labelMargin,
                    y = tickPosDp - textHeightDp / 2
                )
            } else {
                val textWidthDp = with(density) { textLayout.size.width.toDp() }
                Modifier
                    .offset(
                        x = tickPosDp - textWidthDp / 2,
                        y = tickOffset + labelMargin
                    )
                    .graphicsLayer(rotationZ = 45f)
            }

            Text(
                text = tick.label,
                fontSize = 8.sp,
                modifier = modifierText
            )
        }
    }
}

//todo сделать Preview и отрефакторить есть ошибки при постоянном поступлении данных