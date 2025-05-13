package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transfer.domain.utils.DateTimeUtils
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParameterType

@Composable
fun PointDetails(
    position: Offset,
    values: LiftParameters,
    parentSize: IntSize,
    lineColors: List<Color>,
    backgroundColor: Color = Color(0xCC333333)
) {
    if (parentSize == IntSize.Zero) return
    val density = LocalDensity.current
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }

    val marginPx = remember(density) { with(density) { 12.dp.toPx() } }
    val pointerOffsetPx = remember(density) { with(density) { 16.dp.toPx() } }

    val (clampedX, clampedY) = remember(position, parentSize, tooltipSize) {
        val rawX = position.x + pointerOffsetPx
        val rawY = position.y - tooltipSize.height - pointerOffsetPx
        val clampedX = rawX.coerceIn(marginPx, parentSize.width - tooltipSize.width - marginPx)
        val clampedY = rawY.coerceIn(marginPx, parentSize.height - tooltipSize.height - marginPx)
        clampedX to clampedY
    }

    val (xOffsetDp, yOffsetDp) = remember(clampedX, clampedY) {
        with(density) { clampedX.toDp() to clampedY.toDp() }
    }

    val formattedTime = remember(values.timestamp, values.timeMilliseconds) {
        DateTimeUtils.formatTimeWithMillis(
            timeSeconds = values.timestamp,
            timeMilliseconds = values.timeMilliseconds
        )
    }

    Box(
        modifier = Modifier
            .offset(x = xOffsetDp, y = yOffsetDp)
            .onGloballyPositioned { if (tooltipSize != it.size) tooltipSize = it.size }
            .background(backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = formattedTime, fontSize = 12.sp, color = Color.White)
            Spacer(Modifier.height(4.dp))
            values.parameters.forEachIndexed { index, (label, value) ->
                key(index) {
                    PointDetailsRow(
                        label = label,
                        value = value,
                        indicatorColor = lineColors.getOrNull(index) ?: Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun PointDetailsRow(
    label: ParameterType,
    value: Int,
    indicatorColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(indicatorColor, shape = CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text("${label.displayName}: $value", fontSize = 12.sp, color = Color.White)
    }
}


//todo сделать Preview и отрефакторить