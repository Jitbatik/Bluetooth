package com.example.transfer.chart.domain

import com.example.transfer.chart.domain.model.SignalColor
import com.example.transfer.chart.domain.model.ChartSettings
import com.example.transfer.chart.domain.model.SignalSettings


object ChartSettingsDefaults {
    fun getDefault() = ChartSettings(
        title = "Параметры графика",
        description = "Настройте отображение сигналов на графике параметров",
        signals = listOf(
//            SignalSettings(
//                "Time",
//                "Time",
//                0,
//                4,
//                true,
//                SignalColor(255, 0, 0)
//            ),
//            SignalSettings(
//                "MS",
//                "Ms",
//                4,
//                6,
//                true,
//                SignalColor(255, 0, 0)
//            ),       // Красный
            SignalSettings(
                "ENCODER_READINGS",
                "ENCODER_READINGS",
                12,
                16,
                true,
                SignalColor(255, 0, 0)
            ),       // Красный
            SignalSettings(
                "ELEVATOR_SPEED",
                "ELEVATOR_SPEED",
                18,
                20,
                true,
                SignalColor(0, 0, 255)
            ), // Синий
        )
    )
}