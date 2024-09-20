package com.example.bluetooth.presentation.view.connectcontainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ConnectRepository
import com.example.domain.repository.ScannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val scannerRepository: ScannerRepository,
    private val connectRepository: ConnectRepository,
) : ViewModel() {
    private val _isBluetoothEnabled = scannerRepository.isBluetoothActive.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled


    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>>
        get() = _devices.asStateFlow()


    private val _connectedDevice = connectRepository.getConnectedDevice().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice

    init {
        observeBluetoothDeviceList()
    }

    private fun observeBluetoothDeviceList() {
        Log.d(TAG, "Subscribe to a stream device list")
        viewModelScope.launch {
            scannerRepository.deviceList.collect { newDevices ->
                _devices.value = newDevices
            }
        }
    }

    private fun handlerConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        Log.d(TAG, "Handling connection to device")
        viewModelScope.launch {
            if (_connectedDevice.value != null) {
                connectRepository.disconnectFromDevice()
            } else {
                connectRepository.connectToDevice(
                    bluetoothDevice,
                    //connectUUID = "00000000-deca-fade-deca-deafdecacafe"
                    connectUUID = "00001101-0000-1000-8000-00805f9b34fb"
                )
            }

        }
    }

    private fun scan() = viewModelScope.launch {
        try {
            Log.d(TAG, "Scanning for devices")
            scannerRepository.startScan()
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to Scanning devices", exception)
        }
    }

    fun onEvents(event: BTDevicesScreenEvents) {
        when (event) {
            BTDevicesScreenEvents.StartScan -> scan()
            is BTDevicesScreenEvents.ConnectToDevice -> handlerConnectionToDevice(event.device)
        }
    }

    override fun onCleared() {
        scannerRepository.releaseResources()
        connectRepository.releaseResources()
        Log.d(TAG, "CLEARED Resources")
        super.onCleared()
    }

    companion object {
        private val TAG = ConnectViewModel::class.java.simpleName
    }
}

