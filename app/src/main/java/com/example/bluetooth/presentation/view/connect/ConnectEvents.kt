package com.example.bluetooth.presentation.view.connect

import com.example.domain.model.BluetoothDevice

interface ConnectEvents {
    data object StartScan : ConnectEvents
    data object StopScan : ConnectEvents
    data class ConnectToDevice(val device: BluetoothDevice) : ConnectEvents
}