package com.example.bluetooth.model


sealed class ConnectionState {
    data class Connecting(val device: BluetoothDevice) : ConnectionState()
    data class Connected(val device: BluetoothDevice) : ConnectionState()
    data class Disconnected(val device: BluetoothDevice? = null) : ConnectionState()
    data class Error(val device: BluetoothDevice?, val message: String) : ConnectionState()
}