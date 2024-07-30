package com.example.domain.repository

import com.example.domain.model.BluetoothDevice
import kotlinx.coroutines.flow.Flow

/**
    Устройства
        -сохраненные  getSavedBluetoothDevice
        -Новые  scanNewDevice
*/

interface BluetoothRepository {

    fun preparationBluetooth(): Boolean

    fun getPairedDevice() : Flow<List<BluetoothDevice>>

    fun getScannedDevice() : Flow<List<BluetoothDevice>>

    fun stopScan(): Result<Boolean>

    /**
     * Clears resources when done
     */
    fun releaseResources()
}