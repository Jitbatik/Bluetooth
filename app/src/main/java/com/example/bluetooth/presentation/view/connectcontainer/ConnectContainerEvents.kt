package com.example.bluetooth.presentation.view.connectcontainer

import com.example.domain.model.BluetoothDevice

interface ConnectContainerEvents {
    data object StartScan : ConnectContainerEvents
    data object StopScan : ConnectContainerEvents
    data class ConnectToDevice(val device: BluetoothDevice) : ConnectContainerEvents
}