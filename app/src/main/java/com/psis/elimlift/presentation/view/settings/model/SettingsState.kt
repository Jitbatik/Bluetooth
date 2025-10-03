package com.psis.elimlift.presentation.view.settings.model

import com.psis.elimlift.model.ChartSettingsUI

data class SettingsState(
    val chartSettings: ChartSettingsUI,
    val wirelessBluetoothMask: WirelessBluetoothMask,
)
