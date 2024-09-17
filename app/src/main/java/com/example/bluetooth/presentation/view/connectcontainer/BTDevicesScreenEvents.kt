package com.example.bluetooth.presentation.view.connectcontainer

import com.example.domain.model.BluetoothDevice

interface BTDevicesScreenEvents {
    data object StartScan : BTDevicesScreenEvents
    data class ConnectToDevice(val device: BluetoothDevice) : BTDevicesScreenEvents
}