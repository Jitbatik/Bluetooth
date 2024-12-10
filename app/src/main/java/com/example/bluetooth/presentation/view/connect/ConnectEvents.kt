package com.example.bluetooth.presentation.view.connect

import com.example.bluetooth.model.BluetoothDevice


sealed interface ConnectEvents {
    data object StartScan : ConnectEvents
    data object StopScan : ConnectEvents
    data class ConnectToDevice(val device: BluetoothDevice) : ConnectEvents
}