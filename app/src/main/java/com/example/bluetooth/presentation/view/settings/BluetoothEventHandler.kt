package com.example.bluetooth.presentation.view.settings

import com.example.bluetooth.EventHandler
import com.example.bluetooth.domain.SettingsManager
import com.example.bluetooth.presentation.view.settings.model.BluetoothEvent
import javax.inject.Inject

class BluetoothEventHandler @Inject constructor(
    private val settingsManager: SettingsManager
) : EventHandler<BluetoothEvent, Unit> {

    override fun handle(event: BluetoothEvent) {
        when (event) {
            is BluetoothEvent.UpdateEnabled -> settingsManager.saveEnabledChecked(event.isEnabled)
            is BluetoothEvent.UpdateMask -> settingsManager.saveBluetoothMask(event.mask)
        }
    }
}
