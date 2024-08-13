package com.example.data.bluetooth.provider

import android.bluetooth.BluetoothSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothSocketProvider @Inject constructor() {
    private var bluetoothSocket: BluetoothSocket? = null

    fun setSocket(socket: BluetoothSocket) {
        bluetoothSocket = socket
    }

    fun getSocket(): BluetoothSocket? {
        return bluetoothSocket
    }
}
