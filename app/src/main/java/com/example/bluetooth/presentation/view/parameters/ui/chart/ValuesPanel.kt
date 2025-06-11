package com.example.bluetooth.presentation.view.parameters.ui.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ValuesPanel(
    values: List<Pair<Color, Float>>,
    valueFormatter: (Float) -> String,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        if (values.isEmpty()) {
            Text(
                text = "---",
                color = Color.Gray,
                style = MaterialTheme.typography.headlineSmall
            )
        } else {
            values.forEachIndexed { index, (color, value) ->
                key(index) {
                    ValueItem(color = color, value = value, valueFormatter = valueFormatter)
                }
            }
        }
    }
}

@Composable
private fun ValueItem(
    color: Color,
    value: Float,
    valueFormatter: (Float) -> String
) {
    Column {
        Text(
            text = valueFormatter(value),
            color = color,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}