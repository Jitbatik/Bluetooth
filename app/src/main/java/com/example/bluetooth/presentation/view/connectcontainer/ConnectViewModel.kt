package com.example.bluetooth.presentation.view.connectcontainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.view.ConnectContainerUiState
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ConnectRepository
import com.example.domain.repository.ScannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val scannerRepository: ScannerRepository,
    private val connectRepository: ConnectRepository,
) : ViewModel() {
    private val _connectContainerUiState = combine(
        scannerRepository.isBluetoothActive,
        scannerRepository.deviceList,
        connectRepository.getConnectedDevice(),
        scannerRepository.observeScanningState()
    ) { isBluetoothEnabled, devices, connectedDevice, isScanning ->
        ConnectContainerUiState(
            isBluetoothEnabled = isBluetoothEnabled,
            devices = devices,
            connectedDevice = connectedDevice,
            isScanning = isScanning,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ConnectContainerUiState()
    )

    val connectContainerUiState = _connectContainerUiState

    private fun handlerConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        Log.d(TAG, "Handling connection to device")
        viewModelScope.launch {
            if (connectContainerUiState.value.connectedDevice != null) {
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

    private fun startScan() = viewModelScope.launch {
        try {
            Log.d(TAG, "Scanning for devices")
            scannerRepository.startScan()
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to Scanning devices", exception)
        }
    }

    private fun stopScan() = viewModelScope.launch {
        try {
            Log.d(TAG, "Stop Scanning for devices")
            scannerRepository.stopScan()
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to stopped Scanning devices", exception)
        }
    }

    fun onEvents(event: ConnectContainerEvents) {
        when (event) {
            ConnectContainerEvents.StartScan -> startScan()
            ConnectContainerEvents.StopScan -> stopScan()
            is ConnectContainerEvents.ConnectToDevice -> handlerConnectionToDevice(event.device)
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

