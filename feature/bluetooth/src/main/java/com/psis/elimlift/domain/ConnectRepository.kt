package com.psis.elimlift.domain

import android.bluetooth.BluetoothSocket
import com.psis.elimlift.model.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ConnectRepository {
    fun observeSocket(): Flow<Result<BluetoothSocket?>>

    fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean = true,
        scope: CoroutineScope
    )

    fun disconnectFromDevice(): Result<Unit>
    fun releaseResources()
}