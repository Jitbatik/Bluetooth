package com.example.bluetooth.data

import android.content.Context
import com.example.bluetooth.data.utils.BluetoothService
import com.example.bluetooth.data.utils.BluetoothSocketProvider
import com.example.bluetooth.domain.ConnectRepository
import com.example.bluetooth.model.BluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectRepositoryImpl @Inject constructor(
    private val bluetoothService: BluetoothService,
    @ApplicationContext private val context: Context,
    private val bluetoothSocketProvider: BluetoothSocketProvider,
): ConnectRepository {
    override fun getConnectedDevice(): Flow<BluetoothDevice?> {
        TODO("Not yet implemented")
    }

    override suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun disconnectFromDevice(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun releaseResources() {
        TODO("Not yet implemented")
    }
}