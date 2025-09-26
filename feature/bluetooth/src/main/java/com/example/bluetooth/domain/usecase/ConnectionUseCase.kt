package com.example.bluetooth.domain.usecase

import com.example.bluetooth.domain.ConnectRepository
import com.example.bluetooth.model.BluetoothDevice
import com.example.bluetooth.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class ConnectionUseCase @Inject constructor(
    private val repository: ConnectRepository,
) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected())
    fun observeConnection(): Flow<ConnectionState> = _connectionState

    fun connect(device: BluetoothDevice, uuid: String, secure: Boolean, scope: CoroutineScope) {
        val current = _connectionState.value
        if (current is ConnectionState.Connected && current.device.address == device.address) return

        setState(ConnectionState.Connecting(device))
        repository.connectToDevice(device, uuid, secure, scope)

        // подписываемся на изменения сокета
        repository.observeSocket()
            .onEach { result ->
                val socket = result.getOrNull()
                setState(
                    if (socket == null) ConnectionState.Disconnected(currentDevice())
                    else ConnectionState.Connected(device)
                )
            }
            .launchIn(scope)
    }

    fun disconnect() {
        val prevDevice = currentDevice()
        repository.disconnectFromDevice()
        setState(ConnectionState.Disconnected(prevDevice))
    }

    fun releaseResources() {
        repository.releaseResources()
        _connectionState.value = ConnectionState.Disconnected()
    }

    private fun currentDevice(): BluetoothDevice? {
        return when (val state = _connectionState.value) {
            is ConnectionState.Connected -> state.device
            is ConnectionState.Connecting -> state.device
            else -> null
        }
    }
    private fun setState(state: ConnectionState) {
        _connectionState.value = state
    }
}