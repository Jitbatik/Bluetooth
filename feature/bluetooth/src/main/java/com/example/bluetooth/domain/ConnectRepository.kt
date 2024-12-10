package com.example.bluetooth.domain

import com.example.bluetooth.model.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface ConnectRepository {
    fun getConnectedDevice(): Flow<BluetoothDevice?>
    suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean = true,
    ): Result<Boolean>

    fun disconnectFromDevice(): Result<Unit>
    fun releaseResources()
}