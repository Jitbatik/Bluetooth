package com.example.bluetooth.presentation.view.settings.model

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.bluetooth.Event
import com.example.transfer.filePick.ChartXmlFileType

sealed interface SettingsEvent : Event

sealed interface SignalEvent : SettingsEvent {
    data class ToggleSignalVisibility(val signalId: String, val isVisible: Boolean) : SignalEvent
    data class ChangeSignalColor(val signalId: String, val color: Color) : SignalEvent
    data object MakeAllSignalsVisible : SignalEvent
}

sealed interface BluetoothEvent : SettingsEvent {
    data class UpdateEnabled(val isEnabled: Boolean) : BluetoothEvent
    data class UpdateMask(val mask: String) : BluetoothEvent
}

sealed interface ChartXmlPickerEvent : SettingsEvent {
    data class UploadFile(val uri: Uri, val fileType: ChartXmlFileType) : ChartXmlPickerEvent
    data class DeleteFile(val name: String, val fileType: ChartXmlFileType) : ChartXmlPickerEvent
    data class SelectFile(val name: String, val fileType: ChartXmlFileType) : ChartXmlPickerEvent
}