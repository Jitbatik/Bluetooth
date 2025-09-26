package com.example.bluetooth.presentation.view.settings.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.bluetooth.model.ChartSettingsUI
import com.example.bluetooth.model.SignalSettingsUI
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.SignalEvent
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun LineChartSettings(
    chartSettingsUI: ChartSettingsUI,
    onEvents: (SettingsEvent) -> Unit,
) {
    val signals = chartSettingsUI.signals
    if (signals.isEmpty()) return
    ExpandableItem(
        title = chartSettingsUI.title,
    ) {
        Text(
            text = chartSettingsUI.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ToggleAllSignalsButton(
            signals = signals,
            onEvents = onEvents,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        SignalsListWithScrollbar(
            signals = signals,
            onEvents = onEvents,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        )
    }
}

@Composable
private fun ToggleAllSignalsButton(
    signals: List<SignalSettingsUI>,
    onEvents: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasHiddenSignals = signals.any { !it.isVisible }

    FilledTonalButton(
        onClick = {
            signals.forEach { signal ->
                onEvents(SignalEvent.ToggleSignalVisibility(signal.id, hasHiddenSignals))
            }
        },
        modifier = modifier
    ) {
        Text(if (hasHiddenSignals) "Показать все сигналы" else "Скрыть все сигналы")
    }
}

@Composable
private fun SignalsListWithScrollbar(
    signals: List<SignalSettingsUI>,
    onEvents: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Box(modifier) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 12.dp)
        ) {
            items(
                items = signals,
                key = { it.id }
            ) { signal ->
                SignalSettingItem(
                    signal = signal,
                    onVisibilityChanged = { isVisible ->
                        onEvents(SignalEvent.ToggleSignalVisibility(signal.id, isVisible))
                    },
                    onColorChanged = { color ->
                        onEvents(SignalEvent.ChangeSignalColor(signal.id, color))
                    }
                )
            }
        }

        VerticalScrollbarIndicator(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd),
            trackWidth = 4.dp,
            trackPadding = 2.dp
        )
    }
}


@Composable
fun VerticalScrollbarIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.LightGray.copy(alpha = 0.3f),
    thumbColor: Color = Color.DarkGray,
    trackWidth: Dp = 4.dp,
    trackPadding: Dp = 2.dp,
    minThumbHeight: Dp = 40.dp
) {
    val layoutInfo = listState.layoutInfo
    if (layoutInfo.totalItemsCount == 0 || layoutInfo.visibleItemsInfo.isEmpty()) return

    val totalItems = layoutInfo.totalItemsCount
    val firstVisible = listState.firstVisibleItemIndex
    val visibleItems = layoutInfo.visibleItemsInfo.size

    // Размер ползунка
    val proportionVisible = visibleItems.toFloat() / totalItems
    val viewportHeightPx =
        (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
    val scrollbarHeightPx = (viewportHeightPx * proportionVisible).coerceAtLeast(
        with(LocalDensity.current) { minThumbHeight.toPx() }
    )

    // Смещение ползунка
    val maxScrollOffset = (viewportHeightPx - scrollbarHeightPx).coerceAtLeast(0f)
    val scrollFraction = firstVisible.toFloat() / (totalItems - visibleItems).coerceAtLeast(1)
    val scrollbarOffset = scrollFraction * maxScrollOffset

    // Анимации
    val animatedOffset by animateDpAsState(
        targetValue = with(LocalDensity.current) { scrollbarOffset.toDp() },
        label = "scrollbarOffsetAnim"
    )
    val animatedHeight by animateDpAsState(
        targetValue = with(LocalDensity.current) { scrollbarHeightPx.toDp() },
        label = "scrollbarHeightAnim"
    )

    Box(
        modifier
            .fillMaxHeight()
            .width(trackWidth)
            .padding(end = trackPadding)
            .background(trackColor, RoundedCornerShape(trackWidth / 2))
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .offset { IntOffset(x = 0, y = animatedOffset.roundToPx()) }
                .background(thumbColor, RoundedCornerShape(trackWidth / 2))
        )
    }
}


@Composable
private fun SignalSettingItem(
    signal: SignalSettingsUI,
    onVisibilityChanged: (Boolean) -> Unit,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onVisibilityChanged(!signal.isVisible) },
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorIndicator(
                color = signal.color,
                onClick = onColorChanged,
                modifier = Modifier.weight(0.25f)
            )

            Column(
                modifier = Modifier.weight(0.6f)
            ) {
                Text(
                    text = signal.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (signal.isVisible) "Отображается" else "Скрыт",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }

            Icon(
                imageVector = if (signal.isVisible)
                    Icons.Filled.CheckCircle
                else
                    Icons.Outlined.CheckCircle,
                contentDescription = if (signal.isVisible) "Скрыть сигнал" else "Показать сигнал",
                tint = if (signal.isVisible)
                    MaterialTheme.colorScheme.primary
                else
                    textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


object ColorUtils {
    private val hexRegex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})\$".toRegex()

    fun toHex(color: Color): String {
        val alpha = (color.alpha * 255).toInt()
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return "#%02X%02X%02X%02X".format(alpha, red, green, blue)
    }

    fun parseColor(hex: String): Color? {
        if (!hexRegex.matches(hex)) return null
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // Предопределенные цвета в HEX формате
    val defaultColors = listOf(
        "#FF0000", // Red
        "#00FF00", // Green
        "#0000FF", // Blue
        "#FFFF00", // Yellow
        "#FF00FF", // Magenta
        "#00FFFF", // Cyan
        "#FF9800", // Orange
        "#9C27B0", // Purple
        "#2196F3", // Light Blue
        "#009688"  // Teal
    )
}

@Composable
private fun ColorIndicator(
    color: Color,
    onClick: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(4.dp))
            .clickable { showColorPicker = true }
            .then(modifier),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ColorUtils.toHex(color),
                style = MaterialTheme.typography.bodySmall,
                color = if (color.luminance() > 0.5f) Color.Black else Color.White
            )
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = color,
            onColorSelected = { selectedColor ->
                onClick(selectedColor)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hexValue by remember { mutableStateOf(ColorUtils.toHex(initialColor)) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // HEX input
                OutlinedTextField(
                    value = hexValue,
                    onValueChange = { value ->
                        hexValue = value.uppercase()
                        isError = ColorUtils.parseColor(value) == null
                    },
                    label = { Text("HEX код цвета") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Неверный формат. Используйте #RRGGBB или #AARRGGBB") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Color preview
                ColorUtils.parseColor(hexValue)?.let { color ->
                    Surface(
                        color = color,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {}
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = ColorUtils.defaultColors.size,
                        key = { index -> ColorUtils.defaultColors[index] }
                    ) { index ->
                        val hex = ColorUtils.defaultColors[index]
                        ColorUtils.parseColor(hex)?.let { color ->
                            ColorItem(
                                color = color,
                                isSelected = hexValue == hex,
                                onClick = { hexValue = hex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        ColorUtils.parseColor(hexValue)?.let { color ->
                            onColorSelected(color)
                        }
                    },
                    enabled = !isError && ColorUtils.parseColor(hexValue) != null
                ) {
                    Text("Выбрать")
                }
            }
        }
    )
}


@Composable
private fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = color,
        shape = CircleShape,
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {}
}

@PreviewLightDark
@Composable
private fun LineChartSettingsPreview() = BluetoothTheme {
    Surface {
        val previewSettings = ChartSettingsUI(
            title = "График параметров",
            description = "Настройте отображение сигналов на графике",
            signals = listOf(
                SignalSettingsUI("1", "Скорость", true, Color.Red),
                SignalSettingsUI("2", "Ускорение", false, Color.Blue),
                SignalSettingsUI("3", "Положение", true, Color.Green)
            )
        )

        LineChartSettings(
            chartSettingsUI = previewSettings,
            onEvents = {},
        )
    }
}