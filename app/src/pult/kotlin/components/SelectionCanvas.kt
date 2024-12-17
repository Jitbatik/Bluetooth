package components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import com.example.transfer.model.Range

@Composable
fun SelectionCanvas(
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