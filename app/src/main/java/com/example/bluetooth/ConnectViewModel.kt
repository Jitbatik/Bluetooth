package com.example.bluetooth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.BluetoothRepository
import com.example.domain.repository.ConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val bluetoothDeviseRepository: BluetoothRepository,
    private val connectRepository: ConnectRepository
) : ViewModel() {

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices.asStateFlow()

    init {
        fetchDevices()
    }

    private fun fetchDevices() {
        Log.d("ConnectViewModel", "Init Bluetooth")
        //initBluetooth()
        viewModelScope.launch {

        }
    }

    fun handlerConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        Log.d("ConnectViewModel", "Handling connection to device")
        viewModelScope.launch {
            connectRepository.connectToDevice(bluetoothDevice)
        }
    }

    fun findToDevice() {
        Log.d("ConnectViewModel", "Scanning for new devices")
        viewModelScope.launch {
            bluetoothDeviseRepository.getScannedDevice()
                .collect { newDevices ->
                    _devices.value = newDevices
                }
        }
    }
}

