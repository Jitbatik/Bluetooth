package com.example.bluetooth.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.bluetooth.data.receivers.BluetoothConnectedDeviceReceiver
import com.example.bluetooth.data.utils.BluetoothService
import com.example.bluetooth.data.utils.BluetoothSocketProvider
import com.example.bluetooth.domain.ConnectRepository
import com.example.bluetooth.model.BluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class ConnectRepositoryImpl @Inject constructor(
    private val bluetoothService: BluetoothService,
    @ApplicationContext private val context: Context,
    private val bluetoothSocketProvider: BluetoothSocketProvider,
): ConnectRepository {

    private val _bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothService.bluetoothAdapter

    override fun getConnectedDevice(): Flow<BluetoothDevice?> = callbackFlow {
        Log.d(TAG, "Start connection receiver")
        trySend(null)
        val intentFilter = IntentFilter().apply {
            addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED)
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
                bluetoothSocketProvider.updateSocket(socket)
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
            bluetoothSocketProvider.updateSocket(null)
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