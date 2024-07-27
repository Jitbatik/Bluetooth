package com.example.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.domain.model.Device
import com.example.domain.repository.BluetoothDeviseRepository
import javax.inject.Inject

class BluetoothDeviseRepositoryImpl @Inject constructor(): BluetoothDeviseRepository {

    private val deviceList = mutableListOf(
        Device("Device 1", "00:11:22:33:44:55"),
        Device("Device 2", "66:77:88:99:AA:BB")
    )
    //получение списка устройств из бд
    override fun getSavedBluetoothDevise() : LiveData<List<Device>> {
        val liveData = MutableLiveData<List<Device>>()
        liveData.value = deviceList
        // Возвращаем LiveData
        return liveData
    }

    override fun scanNewDevise(): LiveData<List<Device>> {
        deviceList.addAll(listOf(
            Device("Device 3", "00:11:22:33:44:55"),
            Device("Device 4", "66:77:88:99:AA:BB")
        ))
        val liveData = MutableLiveData<List<Device>>()
        liveData.value = deviceList

        // Возвращаем LiveData
        return liveData
    }
}