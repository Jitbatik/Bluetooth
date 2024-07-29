package com.example.domain.repository

import com.example.domain.model.BluetoothDevice

/*
    Действия с устройтвами
        -Подключение  connectToDevice
        -Отключение  disconnectFromDevice
*/

interface ConnectRepository  {

    fun connectToDevice(bluetoothDevice: BluetoothDevice) : Boolean

    fun disconnectFromDevice() : Boolean
}