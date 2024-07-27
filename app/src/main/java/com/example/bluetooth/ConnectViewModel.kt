package com.example.bluetooth

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Device
import com.example.domain.repository.BluetoothDeviseRepository
import com.example.domain.repository.ConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val bluetoothDeviseRepository: BluetoothDeviseRepository,
    private val connectRepository: ConnectRepository
) : ViewModel() {

    private var deviseListLiveData = bluetoothDeviseRepository.getBluetoothDevise()
    private val mediatorLiveData = MediatorLiveData<List<Int>>()
    val deviceList = listOf(
        Device("Device 1", "00:11:22:33:44:55"),
        Device("Device 2", "66:77:88:99:AA:BB")
    )
    init {
        /*mediatorLiveData.addSource(deviseListLiveData){
            mediatorLiveData.value = it
        }*/
    }
    // отключение подключение по нажатию
        // при подключении карточка загарается зеленым
        // при отключении возвращается обратно
    fun handlerConnectionToDevice(deviceName: String) {
        viewModelScope.launch {
            connectRepository.connectToDevise(deviceName = deviceName)
        }
    }

    // сохраненные устройства с новыми
    fun findToDevice() {
        viewModelScope.launch {
            bluetoothDeviseRepository.getBluetoothDevise()
        }
    }

}