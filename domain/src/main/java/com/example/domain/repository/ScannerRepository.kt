package com.example.domain.repository

import com.example.domain.model.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface ScannerRepository {

    val bluetoothDeviceList: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val availableDevices: StateFlow<List<BluetoothDevice>>

    val isBluetoothActive: Flow<Boolean>
    //val hasBluetoothPermission:  Flow<Boolean>

    fun findPairedDevices(): Result<Unit>

    fun startScan(): Result<Boolean>

    fun stopScan(): Result<Boolean>

    fun releaseResources()


}