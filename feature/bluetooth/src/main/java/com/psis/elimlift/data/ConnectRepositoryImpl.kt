package com.psis.elimlift.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.psis.elimlift.data.receivers.BluetoothConnectedDeviceReceiver
import com.psis.elimlift.data.receivers.BondingReceiver
import com.psis.elimlift.data.utils.BluetoothService
import com.psis.elimlift.data.utils.ReceiverManager
import com.psis.elimlift.domain.ConnectRepository
import com.psis.elimlift.model.BluetoothDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
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
        if (!_hasBtPermission || _socket.value.getOrNull() != null && _socket.value.isSuccess) return

        val disconnectReceiver = createDisconnectReceiver(bluetoothDevice)
        receiversManager.register(
            disconnectReceiver,
            IntentFilter(AndroidBluetoothDevice.ACTION_ACL_DISCONNECTED)
        )

        receiversManager.register(
            bluetoothStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )

        scope.launch(Dispatchers.IO) {
            val result = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
                connect(bluetoothDevice, connectUUID, secure)
            } ?: Result.failure(TimeoutException("Connection timeout"))

            _socket.value = result
        }
    }

    override fun disconnectFromDevice(): Result<Unit> = runCatching {
        _socket.value.getOrNull()?.close()
        _socket.value = Result.success(null)
        receiversManager.clear()
    }

    override fun releaseResources() {
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
        val adapter = _bluetoothAdapter
            ?: return Result.failure(IllegalStateException("Bluetooth adapter not available"))
        if (!adapter.isEnabled) return Result.failure(IllegalStateException("Bluetooth is turned off"))
        val device = adapter.getRemoteDevice(bluetoothDevice.address)

        if (secure && !bonded(device)) return Result.failure(IllegalStateException("Device not bonded"))

        val socket = createSocket(device, connectUUID, secure)
            ?: return Result.failure(IllegalStateException("Failed to create socket"))

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
            _socket.value = Result.failure(IllegalStateException("Device connection is broken"))
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_TURNING_OFF,
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d(TAG, "Bluetooth turned off → closing socket")
                        disconnectFromDevice()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluetooth turned on → ready for reconnect")
                        // тут можно вызвать auto-reconnect, если нужно
                    }
                }
            }
        }
    }

    private suspend fun waitForBonding(device: AndroidBluetoothDevice): Boolean =
        withTimeoutOrNull(BONDING_TIMEOUT_MS) {
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
        } ?: false


    companion object {
        private val TAG = ConnectRepositoryImpl::class.java.simpleName
        private const val CONNECT_TIMEOUT_MS = 30_000L // 30 секунд
        private const val BONDING_TIMEOUT_MS = 20_000L // 20 секунд
    }
}