package com.psis.elimlift.presentation.view.connect

import com.psis.elimlift.model.BluetoothDevice


sealed interface ConnectEvents {
    data object StartScan : ConnectEvents
    data object StopScan : ConnectEvents
    data class ConnectToDevice(val device: BluetoothDevice) : ConnectEvents
}