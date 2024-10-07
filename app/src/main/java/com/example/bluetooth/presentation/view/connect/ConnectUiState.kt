package com.example.bluetooth.presentation.view.connect

import com.example.domain.model.BluetoothDevice

data class ConnectUiState(
    val isBluetoothEnabled: Boolean = false,
    val devices: List<BluetoothDevice> = emptyList(),
    val connectedDevice: BluetoothDevice? = null,
    val isScanning: Boolean = false,
)
