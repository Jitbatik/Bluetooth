package com.example.bluetooth.presentation.view.parameters.ui.tooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.parameters.model.DisplayValueWithColor
import com.example.bluetooth.presentation.view.parameters.model.ParameterDisplayData
import com.example.bluetooth.presentation.view.parameters.util.calculateTooltipOffset
import com.example.transfer.protocol.domain.utils.DateTimeUtils

@Composable
fun ChartValueTooltip(
    touchPosition: Offset,
    values: ParameterDisplayData,
    parentSize: IntSize,
    backgroundColor: Color = Color(0xCC333333),
    textStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.White)
) {
    if (parentSize == IntSize.Zero) return

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }

    val marginDp = 12.dp
    val pointerOffsetDp = 16.dp
    val maxTooltipHeightDp = configuration.screenHeightDp.dp * 0.4f

    val marginPx = with(density) { marginDp.toPx() }
    val pointerOffsetPx = with(density) { pointerOffsetDp.toPx() }

    val offset = remember(touchPosition, parentSize, tooltipSize) {
        calculateTooltipOffset(touchPosition, tooltipSize, parentSize, marginPx, pointerOffsetPx)
    }

    val (xOffsetDp, yOffsetDp) = with(density) { offset.x.toDp() to offset.y.toDp() }

    val formattedTime = remember(values.timestamp, values.timeMilliseconds) {
        DateTimeUtils.formatTimeWithMillis(
            timeSeconds = values.timestamp,
            timeMilliseconds = values.timeMilliseconds
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .offset(x = xOffsetDp, y = yOffsetDp)
                .onGloballyPositioned { if (tooltipSize != it.size) tooltipSize = it.size }
                .background(backgroundColor, shape = RoundedCornerShape(4.dp))
                .padding(8.dp)
                .heightIn(max = maxTooltipHeightDp)
        ) {
            TooltipContent(
                formattedTime = formattedTime,
                parameters = values.parameters.toList(),
                textStyle = textStyle,
                maxHeight = maxTooltipHeightDp
            )
        }
    }
}


@Composable
private fun TooltipContent(
    formattedTime: String,
    parameters: List<Pair<String, DisplayValueWithColor>>,
    textStyle: TextStyle,
    maxHeight: Dp
) {
    Column {
        Text(text = formattedTime, style = textStyle)
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier.heightIn(max = maxHeight)
        ) {
            LazyColumn {
                items(parameters) { (label, displayValue) ->
                    PointDetails(
                        label = label,
                        value = displayValue.value.toInt(),
                        indicatorColor = displayValue.color,
                        textStyle = textStyle
                    )
                }
            }
        }
    }
}


@Composable
private fun PointDetails(
    label: String,
    value: Int,
    indicatorColor: Color,
    textStyle: TextStyle
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
        Text("$label: $value", style = textStyle)
    }
}

//todo сделать Preview и отрефакторить