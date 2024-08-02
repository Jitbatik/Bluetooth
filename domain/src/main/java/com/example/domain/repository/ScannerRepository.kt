package com.example.domain.repository

import com.example.domain.model.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface ScannerRepository {

    val deviceList: StateFlow<List<BluetoothDevice>>


    val isBluetoothActive: Flow<Boolean>
    //val hasBluetoothPermission:  Flow<Boolean>


    fun startScan(): Result<Boolean>

    fun releaseResources()


}