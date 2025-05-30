package com.example.bluetooth.data.utils

import android.annotation.SuppressLint
import com.example.bluetooth.model.BluetoothDevice as DomainBluetoothDevice
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice

@SuppressLint("MissingPermission")
fun AndroidBluetoothDevice.toDomainModel(): DomainBluetoothDevice {
    return DomainBluetoothDevice(
        name = this.name ?: "Unknown",
        address = this.address
    )
}