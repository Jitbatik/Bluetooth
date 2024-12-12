package com.example.bluetooth.data.utils

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothSocketProvider @Inject constructor() {
    private val _bluetoothSocket = MutableStateFlow<BluetoothSocket?>(null)
    val bluetoothSocket: StateFlow<BluetoothSocket?> get() = _bluetoothSocket

    fun updateSocket(newSocket: BluetoothSocket?) { _bluetoothSocket.value = newSocket }
//    fun clearSocket() {
//        _bluetoothSocket.value = null
//    }
}