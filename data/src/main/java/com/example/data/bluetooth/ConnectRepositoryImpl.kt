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
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.data.bluetooth.receivers.BluetoothConnectedDeviceReceiver
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ConnectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice


@SuppressLint("MissingPermission")
class ConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothSocketProvider: BluetoothSocketProvider,
) : ConnectRepository {

    private val _bluetoothManager by lazy { context.getSystemService<BluetoothManager>() }
    private val _bluetoothAdapter: BluetoothAdapter?
        get() = _bluetoothManager?.adapter

    override fun getConnectedDevice(): Flow<BluetoothDevice?> = callbackFlow {
        Log.d(TAG, "Start connection receiver")
        trySend(null)
        val intentFilter = IntentFilter().apply {
            addAction(AndroidBluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(AndroidBluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        val receiverConnectedDevice = BluetoothConnectedDeviceReceiver { device ->
            trySend(device)
        }
        ContextCompat.registerReceiver(
            context,
            receiverConnectedDevice,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
        awaitClose {
            context.unregisterReceiver(receiverConnectedDevice)
        }
    }

    private val _hasBtPermission: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PermissionChecker.PERMISSION_GRANTED
        else true

    private var _btClientSocket: BluetoothSocket? = null

    override suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!_hasBtPermission) {
            Log.e(TAG, "No Bluetooth connect permission granted.")
            return@withContext Result.failure(SecurityException("No Bluetooth scan permission granted"))
        }

        val device = _bluetoothAdapter?.getRemoteDevice(bluetoothDevice.address)
            ?: return@withContext Result.failure(SecurityException("No Bluetooth device"))

        if (secure && device.bondState == android.bluetooth.BluetoothDevice.BOND_NONE) {
            if (!device.createBond()) {
                Log.e(TAG, "Failed to bond with device.")
                return@withContext Result.failure(SecurityException("Failed to bond with device"))
            }
        }

        _btClientSocket =
            if (secure) device.createRfcommSocketToServiceRecord(UUID.fromString(connectUUID))
            else device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(connectUUID))
        Log.d(
            TAG,
            "CREATED_SOCKET SECURE: $secure SPECIFIED UUID: $connectUUID"
        )

        if (_bluetoothAdapter?.isDiscovering == true)
            _bluetoothAdapter?.cancelDiscovery()

        return@withContext try {
            _btClientSocket?.let { socket ->
                socket.connect()
                Log.d(TAG, "CLIENT CONNECTED")
                bluetoothSocketProvider.setSocket(socket)
            }
            Result.success(true)
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            Result.failure(e)
        }
    }


    override fun disconnectFromDevice(): Result<Unit> {
        if (_btClientSocket == null) return Result.success(Unit)
        return try {
            _btClientSocket?.close()
            Log.d(TAG, "CLOSING CONNECTION")
            _btClientSocket = null
            bluetoothSocketProvider.setSocket(null)
            Result.success(Unit)
        } catch (e: IOException) {
            Log.d(TAG, "CANNOT CLOSE CONNECTION")
            Result.failure(e)
        }
    }


    override fun releaseResources() {
        Log.d(TAG, "RECEIVER REMOVED")
        try {
            //context.unregisterReceiver(_receiverConnectedDevice)
            //_isConnectReceiverRegistered = false
        } catch (e: Exception) {
            Log.d(TAG, "---------")
        }
    }

    companion object {
        private val TAG = ConnectRepositoryImpl::class.java.simpleName
    }
}


//    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//    private fun fetchUUIDs(address: String): Flow<List<UUID>> = callbackFlow {
//        val device = _bluetoothAdapter?.getRemoteDevice(address)
//
//        val remoteDeviceUUIDReceiver = RemoteDeviceUUIDReceiver(
//            onReceivedUUIDs = { uuids ->
//                // remove the client-server uuid
//                val uuidSet = uuids.distinct()
//                Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "FOUND UUIDS: $uuids")
//                scope.launch { send(uuidSet) }
//            },
//        )
//
//        ContextCompat.registerReceiver(
//            context,
//            remoteDeviceUUIDReceiver,
//            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_UUID),
//            ContextCompat.RECEIVER_EXPORTED
//        )
//        // fetch uuids using service discovery protocol
//        val isOk = device?.fetchUuidsWithSdp()
//        Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "DEVICE UUID FETCH STATUS $isOk")
//        awaitClose {
//            context.unregisterReceiver(remoteDeviceUUIDReceiver)
//            scope.cancel()
//            Log.d(CONNECT_REPOSITORY_IMPL_LOGGER, "RECEIVER_FOR_UUID_REMOVED")
//        }
//    }