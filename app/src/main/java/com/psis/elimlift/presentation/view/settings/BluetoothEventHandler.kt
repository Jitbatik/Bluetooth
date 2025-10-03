package com.psis.elimlift.presentation.view.settings

import com.psis.elimlift.EventHandler
import com.psis.elimlift.domain.SettingsManager
import com.psis.elimlift.presentation.view.settings.model.BluetoothEvent
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
