package com.psis.elimlift.presentation.view.connect

import com.psis.elimlift.model.BluetoothDevice
import com.psis.elimlift.model.ConnectionState


data class ConnectUiState(
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnable: Boolean = false,
    val devices: List<BluetoothDevice> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected(),
    val isScanning: Boolean = false,
)
