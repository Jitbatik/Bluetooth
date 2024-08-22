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

private const val CONNECT_VIEWMODEL = "CONNECT_VIEWMODEL"

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

    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice: StateFlow<BluetoothDevice?>
        get() = _connectedDevice.asStateFlow()

    init {
        observeBluetoothDeviceList()
    }

    private fun observeBluetoothDeviceList() {
        Log.d(CONNECT_VIEWMODEL, "Subscribe to a stream device list")
        viewModelScope.launch {
            scannerRepository.deviceList.collect { newDevices ->
                _devices.value = newDevices
            }
        }
    }

    fun handlerConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        Log.d(CONNECT_VIEWMODEL, "Handling connection to device")
        viewModelScope.launch {
            if (_connectedDevice.value != null) {
                connectRepository.disconnectFromDevice()
                _connectedDevice.value = null
            }
            else {
                connectRepository.connectToDevice(
                    bluetoothDevice,
                    connectUUID = "00000000-deca-fade-deca-deafdecacafe"
                    //connectUUID = "00001101-0000-1000-8000-00805f9b34fb"
                )
                _connectedDevice.value = bluetoothDevice
            }

        }
    }


    fun scan() = viewModelScope.launch {
        try {
            Log.d(CONNECT_VIEWMODEL, "Scanning for devices")
            scannerRepository.startScan()
        } catch (exception: Exception) {
            Log.e("ConnectViewModel", "Failed to Scanning devices", exception)
        }
    }
}

