package com.example.bluetooth.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.bluetooth.data.receivers.BluetoothConnectedDeviceReceiver
import com.example.bluetooth.data.receivers.BondingReceiver
import com.example.bluetooth.data.utils.BluetoothService
import com.example.bluetooth.data.utils.ReceiverManager
import com.example.bluetooth.domain.ConnectRepository
import com.example.bluetooth.model.BluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice


@SuppressLint("MissingPermission")
class ConnectRepositoryImpl @Inject constructor(
    private val bluetoothService: BluetoothService,
    @ApplicationContext private val context: Context,
    private val receiversManager: ReceiverManager
) : ConnectRepository {

    private val _bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothService.bluetoothAdapter

    private val _hasBtPermission: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PermissionChecker.PERMISSION_GRANTED
        else true


    private val _socket = MutableStateFlow<Result<BluetoothSocket?>>(Result.success(null))
    override fun observeSocket(): Flow<Result<BluetoothSocket?>> = _socket

    override fun connectToDevice(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean,
        scope: CoroutineScope
    ) {
        if (!_hasBtPermission || _socket.value.getOrNull() != null) return


        val disconnectReceiver = createDisconnectReceiver(bluetoothDevice)

        receiversManager.register(
            disconnectReceiver,
            IntentFilter(AndroidBluetoothDevice.ACTION_ACL_DISCONNECTED)
        )

        scope.launch(Dispatchers.IO) {
            try {
                val result = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
                    connect(bluetoothDevice, connectUUID, secure)
                } ?: Result.failure(TimeoutException("Connection timeout"))

                _socket.value = result

                if (result.isFailure) {
                    disconnectFromDevice()
                }
            } finally {
                receiversManager.unregister(disconnectReceiver)
            }
        }
    }

    override fun disconnectFromDevice(): Result<Unit> = runCatching {
        _socket.value.getOrNull()?.close()
        _socket.value = Result.success(null)
    }

    override fun releaseResources() {
        disconnectFromDevice()
        receiversManager.clear()
        Log.d(TAG, "RECEIVER REMOVED")
    }

    // -------------------------------
    // Private helpers
    // -------------------------------

    private suspend fun connect(
        bluetoothDevice: BluetoothDevice,
        connectUUID: String,
        secure: Boolean,
    ): Result<BluetoothSocket> {
        val device = _bluetoothAdapter?.getRemoteDevice(bluetoothDevice.address)
            ?: throw IllegalStateException("Bluetooth adapter not available")

        if (secure && !bonded(device)) throw IllegalStateException("Device not bonded")

        val socket = createSocket(device, connectUUID, secure)
            ?: throw IOException("Failed to create socket")

        // Отменяем discovery, чтобы не мешал подключению
        _bluetoothAdapter?.takeIf { it.isDiscovering }?.cancelDiscovery()

        return runCatching {
            socket.connect()
            socket
        }
    }


    private suspend fun bonded(device: AndroidBluetoothDevice): Boolean =
        when (device.bondState) {
            AndroidBluetoothDevice.BOND_BONDED -> true
            AndroidBluetoothDevice.BOND_BONDING -> waitForBonding(device)
            else -> device.createBond() && waitForBonding(device)
        }

    private fun createSocket(
        device: AndroidBluetoothDevice,
        connectUUID: String,
        secure: Boolean
    ): BluetoothSocket? {
        val uuid = UUID.fromString(connectUUID)
        return if (secure) device.createRfcommSocketToServiceRecord(uuid)
        else device.createInsecureRfcommSocketToServiceRecord(uuid)
    }



    private fun createDisconnectReceiver(
        bluetoothDevice: BluetoothDevice
    ): BroadcastReceiver = BluetoothConnectedDeviceReceiver { disconnectedDevice ->
        if (disconnectedDevice?.address == bluetoothDevice.address) {
            _socket.value = Result.success(null)
        }
    }

    private suspend fun waitForBonding(device: AndroidBluetoothDevice): Boolean =
        suspendCancellableCoroutine { cont ->
            val receiver = BondingReceiver(device, cont)

            receiversManager.register(
                receiver,
                IntentFilter(AndroidBluetoothDevice.ACTION_BOND_STATE_CHANGED)
            )

            cont.invokeOnCancellation {
                receiversManager.unregister(receiver)
            }
        }

    companion object {
        private val TAG = ConnectRepositoryImpl::class.java.simpleName
        private const val CONNECT_TIMEOUT_MS = 30_000L // 30 секунд
    }
}