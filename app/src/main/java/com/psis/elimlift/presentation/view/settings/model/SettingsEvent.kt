package com.psis.elimlift.presentation.view.settings.model

import androidx.compose.ui.graphics.Color
import com.psis.elimlift.Event

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
