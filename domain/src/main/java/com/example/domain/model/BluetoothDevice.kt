package com.example.domain.model

data class BluetoothDevice(
    val name: String = "",
    val address: String = "",
    val isConnected: Boolean = false
)