package com.example.bluetooth.presentation.view.connect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.domain.ScannerRepository
import com.example.bluetooth.domain.usecase.ConnectionUseCase
import com.example.bluetooth.model.BluetoothDevice
import com.example.bluetooth.model.ConnectionState
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
    private val connectUseCase: ConnectionUseCase,
) : ViewModel() {
    private val tag = ConnectViewModel::class.java.simpleName

    private val _connectUiState = combine(
        scannerRepository.isBluetoothActive.distinctUntilChanged(),
        scannerRepository.isLocationActive.distinctUntilChanged(),
        scannerRepository.deviceList,
        connectUseCase.observeConnection(viewModelScope),
        scannerRepository.observeScanningState().distinctUntilChanged(),
    ) { isBluetoothEnabled, isLocationEnable, devices, connectionState, isScanning ->
        ConnectUiState(
            isBluetoothEnabled = isBluetoothEnabled,
            isLocationEnable = isLocationEnable,
            devices = devices,
            connectionState = connectionState,
            isScanning = isScanning,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ConnectUiState(
            isBluetoothEnabled = false,
            isLocationEnable = false,
            devices = emptyList(),
            connectionState = ConnectionState.Disconnected(),
            isScanning = false,
        )
    )
    val connectContainerUiState = _connectUiState

    private fun handlerConnectionToDevice(bluetoothDevice: BluetoothDevice) {
        Log.d(tag, "Handling connection to device")
        viewModelScope.launch {
            when (_connectUiState.value.connectionState) {
                is ConnectionState.Connected -> {
                    connectUseCase.disconnect()
                }

                else -> {
                    connectUseCase.connect(
                        device = bluetoothDevice,
                        uuid = "00001101-0000-1000-8000-00805f9b34fb",
                        secure = true,
                        scope = viewModelScope
                    )
                }
            }
        }
    }

    private fun startScan() = viewModelScope.launch {
        try {
            Log.d(tag, "Scanning for devices")
            scannerRepository.startScan()
            // TODO  а надо ли стирать статусы с прошлого листа?
//            connectUseCase.releaseResources()
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
        connectUseCase.releaseResources()
        Log.d(tag, "CLEARED Resources")
        super.onCleared()
    }
}