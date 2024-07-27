package com.example.bluetooth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Device
import com.example.domain.repository.BluetoothDeviseRepository
import com.example.domain.repository.ConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val bluetoothDeviseRepository: BluetoothDeviseRepository,
    private val connectRepository: ConnectRepository
) : ViewModel() {

    private var deviseListLiveData : LiveData<List<Device>> = bluetoothDeviseRepository.getSavedBluetoothDevise()
    val mediatorLiveData = MediatorLiveData<List<Device>>()

    init {
        mediatorLiveData.addSource(deviseListLiveData) { devices ->
            mediatorLiveData.value = devices
        }
    }

    // отключение подключение по нажатию
        // при подключении карточка загарается зеленым
        // при отключении возвращается обратно
    fun handlerConnectionToDevice(device: Device) {
        viewModelScope.launch {
            connectRepository.connectToDevise(device = device)
        }
    }

    // сохраненные устройства с новыми
    fun findToDevice() {
        viewModelScope.launch {
            val newDevices = bluetoothDeviseRepository.scanNewDevise().value ?: emptyList()
            withContext(Dispatchers.Main) {
                mediatorLiveData.postValue(newDevices)
            }
        }
    }
}