package com.example.domain.repository

import com.example.domain.model.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface BluetoothRepository {
    val bluetoothDeviceList: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val availableDevices: StateFlow<List<BluetoothDevice>>

    fun findPairedDevices(): Result<Unit>

    fun startScan(): Result<Boolean>

    fun stopScan(): Result<Boolean>

    fun releaseResources()


}