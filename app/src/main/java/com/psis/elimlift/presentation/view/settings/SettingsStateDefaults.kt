package com.psis.elimlift.presentation.view.settings

import com.psis.elimlift.model.ChartSettingsUI
import com.psis.elimlift.presentation.view.settings.model.SettingsState
import com.psis.elimlift.presentation.view.settings.model.WirelessBluetoothMask

object SettingsStateDefaults {
    fun getDefault() = SettingsState(
        chartSettings = ChartSettingsUI(
            title = "",
            description = "",
            signals = emptyList()
        ),
        wirelessBluetoothMask = WirelessBluetoothMask(
            isEnabled = false,
            mask = ""
        ),
    )
}