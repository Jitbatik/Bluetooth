package com.psis.elimlift.presentation.view.parameters.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.absoluteValue


@Composable
fun RangeLabel(
    minValue: Float?,
    maxValue: Float?,
    color: Color,
    style: TextStyle,
    decimalPlaces: Int = 2,
    modifier: Modifier = Modifier
) {
    val displayText = if (minValue != null && maxValue != null) {
        "${minValue.formatSafe(decimalPlaces)} - ${maxValue.formatSafe(decimalPlaces)}"
    } else "---"


    Box(
        modifier = modifier.wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = color,
            style = style,
            modifier = Modifier

        )
    }
}
//                .vertical()
//                .rotate(-90f)
fun Modifier.vertical() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.placeRelative(
            x = (placeable.height - placeable.width) / 2,
            y = (placeable.width - placeable.height) / 2
        )
    }
}

fun Float.formatSafe(decimalPlaces: Int): String {
    if (this == 0f) return "0.${"0".repeat(decimalPlaces)}"

    val absValue = this.absoluteValue
    val pattern = if (absValue in 0.001..1e7) {
        buildString { append("#."); repeat(decimalPlaces) { append("#") } }
    } else {
        buildString { append("0."); repeat(decimalPlaces) { append("#") }; append("E0") }
    }

    val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
    return df.format(this)
}
