package com.example.bluetooth.presentation.view.settings.model

import androidx.compose.ui.graphics.Color

sealed interface SettingsEvent {
    data class ToggleSignalVisibility(val signalId: String, val isVisible: Boolean) : SettingsEvent
    data class ChangeSignalColor(val signalId: String, val color: Color) : SettingsEvent
    data object MakeAllSignalsVisible : SettingsEvent

    data class UpdateEnabled(val isEnabled: Boolean) : SettingsEvent
    data class UpdateMask(val mask: String) : SettingsEvent
}