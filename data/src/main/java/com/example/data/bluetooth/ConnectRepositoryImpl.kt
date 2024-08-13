package com.example.data.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.getSystemService
import com.example.data.bluetooth.receivers.RemoteDeviceUUIDReceiver
import com.example.domain.enums.ClientConnectionState
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ConnectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val CONNECT_REPOSITORY_IMPL_LOGGER = "CONNECT_REPOSITORY_IMPL_LOGGER"

@SuppressLint("MissingPermission")
class ConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
): ConnectRepository {

    private val _bluetoothManager by lazy { context.getSystemService<BluetoothManager>() }
    private val _bluetoothAdapter: BluetoothAdapter?
        get() = _bluetoothManager?.adapter


    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private fun fetchUUIDs(address: String): Flow<List<UUID>> = callbackFlow {
        val device = _bluetoothAdapter?.getRemoteDevice(address)

        val remoteDeviceUUIDReceiver = RemoteDeviceUUIDReceiver(
            onReceivedUUIDs = { uuids ->
                // remove the client-server uuid
                val uuidSet = uuids.distinct()
                Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "FOUND UUIDS: $uuids")
                scope.launch { send(uuidSet) }
            },
        )

        ContextCompat.registerReceiver(
            context,
            remoteDeviceUUIDReceiver,
            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_UUID),
            ContextCompat.RECEIVER_EXPORTED
        )
        // fetch uuids using service discovery protocol
        val isOk = device?.fetchUuidsWithSdp()
        Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "DEVICE UUID FETCH STATUS $isOk")
        awaitClose {
            context.unregisterReceiver(remoteDeviceUUIDReceiver)
            scope.cancel()
            Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "RECEIVER_FOR_UUID_REMOVED")
        }
    }


    private val _hasBtPermission: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PermissionChecker.PERMISSION_GRANTED
        else true

    private val _connectState = MutableStateFlow(ClientConnectionState.CONNECTION_INITIALIZING)
    private var _btClientSocket: BluetoothSocket? = null
    override suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean
    ): Result<BluetoothSocket> = withContext(Dispatchers.IO){
        if (!_hasBtPermission) {
            Log.e(CONNECT_REPOSITORY_IMPL_LOGGER, "No Bluetooth connect permission granted.")
            return@withContext Result.failure(SecurityException("No Bluetooth scan permission granted"))
        }

        val device = _bluetoothAdapter?.getRemoteDevice(bluetoothDevice.address)
            ?: return@withContext Result.failure(SecurityException("No Bluetooth device"))

        if (secure && device.bondState == android.bluetooth.BluetoothDevice.BOND_NONE) {
            if (!device.createBond()) {
                Log.e(CONNECT_REPOSITORY_IMPL_LOGGER, "Failed to bond with device.")
                return@withContext Result.failure(SecurityException("Failed to bond with device"))
            }
            _connectState.update { ClientConnectionState.CONNECTION_BONDED }
        }

        _btClientSocket = if (secure) device.createRfcommSocketToServiceRecord(UUID.fromString(connectUUID))
        else device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(connectUUID))
        Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "CREATED_SOCKET SECURE: $secure SPECIFIED UUID: $connectUUID")

        if (_bluetoothAdapter?.isDiscovering == true)
            _bluetoothAdapter?.cancelDiscovery()

        return@withContext try {
            _btClientSocket?.let { socket ->
                socket.connect()
                Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "CLIENT CONNECTED")
                _connectState.update { ClientConnectionState.CONNECTION_ACCEPTED }
                Result.success(socket)
            } ?: Result.failure(IOException("Socket is null"))
        } catch (e: IOException) {
            Log.e(CONNECT_REPOSITORY_IMPL_LOGGER, "Connection failed: ${e.message}")
            _connectState.update { ClientConnectionState.CONNECTION_DENIED }
            Result.failure(e)
        }
    }


    override fun disconnectFromDevice(): Result<Unit> {
        if (_btClientSocket == null) return Result.success(Unit)
        return try {
            _btClientSocket?.close()
            _connectState.update { ClientConnectionState.CONNECTION_DISCONNECTED }
            Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "CLOSING CONNECTION")
            _btClientSocket = null

            Result.success(Unit)
        } catch (e: IOException) {
            Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "CANNOT CLOSE CONNECTION")
            Result.failure(e)
        }
    }


}