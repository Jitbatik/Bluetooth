package com.psis.elimlift.presentation.view.settings.utils

import androidx.compose.ui.graphics.Color
import com.psis.elimlift.model.ChartSettingsUI
import com.psis.elimlift.model.SignalSettingsUI
import com.psis.transfer.chart.domain.model.SignalUserSettings

fun List<SignalUserSettings>.userChartSettingsMapToUI(): ChartSettingsUI {
    return ChartSettingsUI(
        title = "Отображение графиков",
        description = "Настройте отображение сигналов на графике состояний",
        signals = this.map { signal ->
            SignalSettingsUI(
                name = signal.name,
                comment = signal.name,
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