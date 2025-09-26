package com.example.bluetooth.presentation.view.connect

import com.example.bluetooth.model.BluetoothDevice
import com.example.bluetooth.model.ConnectionState


data class ConnectUiState(
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnable: Boolean = false,
    val devices: List<BluetoothDevice> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected(),
    val isScanning: Boolean = false,
)
