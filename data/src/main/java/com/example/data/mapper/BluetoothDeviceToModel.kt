package com.example.data.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice
import com.example.domain.model.BluetoothDevice as DomainBluetoothDevice

@SuppressLint("MissingPermission")
fun AndroidBluetoothDevice.toDomainModel(): DomainBluetoothDevice {
    return DomainBluetoothDevice(
        name = this.name ?: "Unknown",
        address = this.address
    )
}