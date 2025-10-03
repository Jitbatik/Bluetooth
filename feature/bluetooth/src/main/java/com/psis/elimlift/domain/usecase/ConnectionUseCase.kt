package com.psis.elimlift.domain.usecase

import android.bluetooth.BluetoothSocket
import com.psis.elimlift.domain.ConnectRepository
import com.psis.elimlift.model.BluetoothDevice
import com.psis.elimlift.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class ConnectionUseCase @Inject constructor(
    private val repository: ConnectRepository,
) {
    private val _connectionState =
        MutableStateFlow<ConnectionState>(ConnectionState.Disconnected())

    fun observeConnection(scope: CoroutineScope): StateFlow<ConnectionState> {
        repository.observeSocket()
            .mapToConnectionState()
            .onEach { _connectionState.value = it }
            .launchIn(scope)

        return _connectionState
    }

    private var currentDevice: BluetoothDevice? = null

    private fun Flow<Result<BluetoothSocket?>>.mapToConnectionState(): Flow<ConnectionState> {
        return this.map { result ->
            val device = currentDevice
            when {
                result.isFailure -> {
                    val message = result.exceptionOrNull()?.message ?: "Unknown error"
                    ConnectionState.Error(device, message)
                }

                result.isSuccess -> {
                    val socket = result.getOrNull()
                    if (socket != null && device != null) {
                        ConnectionState.Connected(device)
                    } else {
                        ConnectionState.Disconnected(device)
                    }
                }

                else -> ConnectionState.Disconnected(device)
            }
        }
    }

    fun connect(
        device: BluetoothDevice,
        uuid: String,
        secure: Boolean,
        scope: CoroutineScope
    ) {
        currentDevice = device
        _connectionState.value = ConnectionState.Connecting(device)
        repository.connectToDevice(device, uuid, secure, scope)
    }


    fun disconnect() {
        val prevDevice = currentDevice
        repository.disconnectFromDevice()
        _connectionState.value = ConnectionState.Disconnected(prevDevice)
        repository.releaseResources()
    }

    fun releaseResources() {
        repository.releaseResources()
        _connectionState.value = ConnectionState.Disconnected()
    }
}