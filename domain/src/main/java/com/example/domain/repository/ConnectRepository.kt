package com.example.domain.repository

import com.example.domain.model.BluetoothDevice
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