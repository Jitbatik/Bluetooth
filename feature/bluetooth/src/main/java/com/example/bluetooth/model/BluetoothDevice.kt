package com.example.bluetooth.model

data class BluetoothDevice(
    val name: String = "",
    val address: String = "",
    val rssi: Int = Int.MIN_VALUE
)
