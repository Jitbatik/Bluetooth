package com.example.bluetooth.presentation.view.settings.model

import com.example.bluetooth.model.ChartSettingsUI
import com.example.transfer.filePick.domain.FilesMetadata

data class SettingsState(
    val chartSettings: ChartSettingsUI,
    val wirelessBluetoothMask: WirelessBluetoothMask,
    val commonFiles: FilesMetadata?,
    val versionFiles: List<FilesMetadata>,
    val selectedFileName: String?,
)
