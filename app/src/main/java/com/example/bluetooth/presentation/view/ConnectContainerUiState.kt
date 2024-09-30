package com.example.bluetooth.presentation.view

import com.example.domain.model.BluetoothDevice

data class ConnectContainerUiState(
    val isBluetoothEnabled: Boolean = false,
    val devices: List<BluetoothDevice> = emptyList(),
    val connectedDevice: BluetoothDevice? = null,
    val isScanning: Boolean = false,
)
