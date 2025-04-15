package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import com.example.transfer.model.ParameterPoint

@Composable
fun DrawGraph(
    parameters: List<ParameterPoint>,
    minXAxis: Int,
    stepSizeXAxis: Float,
    stepSizeYAxis: Float,
    stepCounterXAxis: Int,
    onStepSizeXAxisChange: (Float) -> Unit,
    onStepSizeYAxisChange: (Float) -> Unit,
    modifier: Modifier,
    lineColor: Color = Color.Red,
    lineWeight: Float = 4f,
) {
    val minYAxis = parameters.minOf { it.value }
    val maxValue = parameters.maxOf { it.value }

    Canvas(
        modifier = modifier
    ) {
        onStepSizeYAxisChange(size.height / (maxValue - minYAxis))
        onStepSizeXAxisChange(size.width / stepCounterXAxis)

        val points = mutableListOf<Offset>()
        parameters.forEach {
            points.add(
                Offset(
                    (it.timeStamp - minXAxis) * stepSizeXAxis,
                    size.height - (it.value - minYAxis) * stepSizeYAxis
                )
            )
        }

        val path = Path().apply {
            points.firstOrNull()?.let { moveTo(it.x, it.y) }
            points.forEach { lineTo(it.x, it.y) }
        }

        clipRect {
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = lineWeight)
            )
        }
    }
}

//todo сделать Preview и отрефакторить