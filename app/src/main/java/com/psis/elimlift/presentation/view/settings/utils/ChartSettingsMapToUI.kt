package com.psis.elimlift.presentation.view.settings.utils

import androidx.compose.ui.graphics.Color
import com.psis.transfer.chart.domain.model.ChartSettings
import com.psis.elimlift.model.ChartSettingsUI
import com.psis.elimlift.model.SignalSettingsUI

fun ChartSettings.chartSettingsMapToUI(): ChartSettingsUI {
    return ChartSettingsUI(
        title = title,
        description = description,
        signals = config.signals.map { signal ->
            SignalSettingsUI(
                id = signal.name,
                name = signal.comment,
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
