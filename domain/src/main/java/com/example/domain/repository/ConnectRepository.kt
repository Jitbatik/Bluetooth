package com.example.domain.repository

import com.example.domain.model.BluetoothDevice

interface ConnectRepository  {

    suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean = true
    ) : Result<Unit>

    fun disconnectFromDevice() : Result<Unit>
}