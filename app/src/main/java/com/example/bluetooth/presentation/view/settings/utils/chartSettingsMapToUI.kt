package com.example.bluetooth.presentation.view.settings.utils

import androidx.compose.ui.graphics.Color
import com.example.transfer.chart.domain.model.ChartSettings
import com.example.bluetooth.model.ChartSettingsUI
import com.example.bluetooth.model.SignalSettingsUI

fun ChartSettings.chartSettingsMapToUI(): ChartSettingsUI {
    return ChartSettingsUI(
        title = title,
        description = description,
        signals = signals.map { signal ->
            SignalSettingsUI(
                id = signal.id,
                name = signal.name,
                isVisible = signal.isVisible,
                color = Color(
                    signal.color.red / 255f,
                    signal.color.green / 255f,
                    signal.color.blue / 255f
                )
            )
        }
    )
}
