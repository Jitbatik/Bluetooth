package com.example.data

import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ConnectRepository
import javax.inject.Inject

class ConnectRepositoryImpl @Inject constructor(): ConnectRepository {

    //подключение к выбранному девайсу
    override fun connectToDevice(bluetoothDevice: BluetoothDevice): Boolean {
        TODO("Not yet implemented")
        return true
    }

    // отключение от устройства
    override fun disconnectFromDevice(): Boolean {
        TODO("Not yet implemented")
    }


}