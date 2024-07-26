package com.example.data

import com.example.domain.BluetoothDeviseRepository
import javax.inject.Inject

class BluetoothDeviseRepositoryImpl @Inject constructor(): BluetoothDeviseRepository {

    //получение списка устройств
    override fun getBluetoothDevise() : List<String> {
        return emptyList()
    }
}