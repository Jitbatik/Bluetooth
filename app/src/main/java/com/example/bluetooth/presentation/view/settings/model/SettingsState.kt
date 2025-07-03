package com.example.bluetooth.presentation.view.settings.model

import com.example.bluetooth.model.ChartSettingsUI

data class SettingsState(
    val chartSettings: ChartSettingsUI,
    val wirelessBluetoothMask: WirelessBluetoothMask,
)
