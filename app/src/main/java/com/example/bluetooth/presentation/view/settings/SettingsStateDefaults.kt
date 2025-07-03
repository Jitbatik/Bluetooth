package com.example.bluetooth.presentation.view.settings

import com.example.bluetooth.model.ChartSettingsUI
import com.example.bluetooth.presentation.view.settings.model.SettingsState
import com.example.bluetooth.presentation.view.settings.model.WirelessBluetoothMask

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