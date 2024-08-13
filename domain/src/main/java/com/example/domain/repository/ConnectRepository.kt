package com.example.domain.repository

import android.bluetooth.BluetoothSocket
import com.example.domain.model.BluetoothDevice

interface ConnectRepository  {

    suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean = true
    ) : Result<BluetoothSocket>

    fun disconnectFromDevice() : Result<Unit>
}