package com.example.bluetooth.presentation.view.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ConnectRepository
import com.example.domain.repository.ScannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val scannerRepository: ScannerRepository,
    private val connectRepository: ConnectRepository,
) : ViewModel() {
    private val tag = ConnectViewModel::class.java.simpleName

    private val _connectUiState = combine(
        scannerRepository.isBluetoothActive.distinctUntilChanged(),
        scannerRepository.deviceList,
        connectRepository.getConnectedDevice().distinctUntilChanged(),
        scannerRepository.observeScanningState().distinctUntilChanged(),
    ) { isBluetoothEnabled, devices, connectedDevice, isScanning ->
        ConnectUiState(
            isBluetoothEnabled = isBluetoothEnabled,
            devices = devices,
            connectedDevice = connectedDevice,
            isScanning = isScanning,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ConnectUiState()
    )
    val connectContainerUiState = _connectUiState

    private fun handlerConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        Log.d(tag, "Handling connection to device")
        viewModelScope.launch {
            if (connectContainerUiState.value.connectedDevice != null) {
                connectRepository.disconnectFromDevice()
            } else {
                connectRepository.connectToDevice(
                    bluetoothDevice,
                    connectUUID = "00001101-0000-1000-8000-00805f9b34fb"
                )
            }

        }
    }

    private fun startScan() = viewModelScope.launch {
        try {
            Log.d(tag, "Scanning for devices")
            scannerRepository.startScan()
        } catch (exception: Exception) {
            Log.e(tag, "Failed to Scanning devices", exception)
        }
    }

    private fun stopScan() = viewModelScope.launch {
        try {
            Log.d(tag, "Stop Scanning for devices")
            scannerRepository.stopScan()
        } catch (exception: Exception) {
            Log.e(tag, "Failed to stopped Scanning devices", exception)
        }
    }

    fun onEvents(event: ConnectEvents) {
        when (event) {
            ConnectEvents.StartScan -> startScan()
            ConnectEvents.StopScan -> stopScan()
            is ConnectEvents.ConnectToDevice -> handlerConnectionToDevice(event.device)
        }
    }

    override fun onCleared() {
        scannerRepository.releaseResources()
        connectRepository.releaseResources()
        Log.d(tag, "CLEARED Resources")
        super.onCleared()
    }
}

