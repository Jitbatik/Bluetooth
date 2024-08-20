package com.example.data.bluetooth.provider

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothSocketProvider @Inject constructor() {
    private val _bluetoothSocket = MutableStateFlow<BluetoothSocket?>(null)
    val bluetoothSocket: StateFlow<BluetoothSocket?> get() = _bluetoothSocket

    fun setSocket(socket: BluetoothSocket?) {
        Log.d("PPP", "init socketProvider")
        _bluetoothSocket.value = socket
    }
}
