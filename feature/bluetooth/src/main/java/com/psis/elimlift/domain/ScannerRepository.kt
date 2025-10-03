package com.psis.elimlift.domain

import com.psis.elimlift.model.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ScannerRepository {
    val deviceList: StateFlow<List<BluetoothDevice>>
    val isBluetoothActive: Flow<Boolean>
    val isLocationActive: Flow<Boolean>

    fun observeScanningState(): Flow<Boolean>
    fun startScan(): Result<Boolean>
    fun stopScan(): Result<Boolean>
    fun releaseResources()
}