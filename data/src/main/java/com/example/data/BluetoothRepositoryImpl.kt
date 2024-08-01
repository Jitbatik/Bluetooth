package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.getSystemService
import com.example.data.receivers.BluetoothStateReceiver
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ScannerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

private typealias BluetoothDeviceModels = List<BluetoothDevice>
private const val BLUETOOTH_SCANNER = "BLUETOOTH_REPOSITORY"

@SuppressLint("MissingPermission")
class ScannerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ScannerRepository {

    // _pairedDevices + _availableDevices ||| single device
    private val _bluetoothDeviceList = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    override val bluetoothDeviceList: StateFlow<List<BluetoothDevice>>
        get() = _bluetoothDeviceList.asStateFlow()

    private val _pairedDevices = MutableStateFlow<BluetoothDeviceModels>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDevice>>
        get() = _pairedDevices.asStateFlow()

    private val _availableDevices = MutableStateFlow<BluetoothDeviceModels>(emptyList())
    override val availableDevices: StateFlow<BluetoothDeviceModels>
        get() = _availableDevices.asStateFlow()


    private val _bluetoothManager by lazy { context.getSystemService<BluetoothManager>() }
    private val _bluetoothAdapter: BluetoothAdapter?
        get() = _bluetoothManager?.adapter



    private val _isBluetoothActive: Boolean
        get() = _bluetoothAdapter?.isEnabled ?: false

    override val isBluetoothActive: Flow<Boolean>
        get() = callbackFlow {
            trySend(_isBluetoothActive)
            val btModeReceiver = BluetoothStateReceiver { isActive ->
                trySend(isActive)
            }

            ContextCompat.registerReceiver(
                context,
                btModeReceiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED
            )

            awaitClose {
                context.unregisterReceiver(btModeReceiver)
            }
        }

    private val _hasScanPermission: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        else true

    private val _hasLocationPermission: Boolean
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        else true

    override fun findPairedDevices(): Result<Unit> {
        if (!_hasScanPermission) {
            Log.e(BLUETOOTH_SCANNER, "No Bluetooth scan permission granted.")
            return Result.failure(SecurityException("No Bluetooth scan permission granted"))
        }

        Log.d(BLUETOOTH_SCANNER, "Subscribe to a stream")
        return try {
            val pairedDevices = _bluetoothAdapter?.bondedDevices ?: emptySet()
            val devicesList = pairedDevices.map { device ->
                BluetoothDevice(
                    name = device.name,
                    address = device.address
                )
            }
            _bluetoothDeviceList.value = devicesList
            _pairedDevices.value = devicesList
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun startScan(): Result<Boolean> {
        if (!_hasScanPermission) {
            Log.e(BLUETOOTH_SCANNER, "No Bluetooth scan permission granted.")
            return Result.failure(SecurityException("No Bluetooth scan permission granted"))
        }
        if (!_hasLocationPermission) {
            Log.e(BLUETOOTH_SCANNER, "No Bluetooth Location permission granted.")
            return Result.failure(SecurityException("No Bluetooth Location permission granted"))
        }
        Log.d(BLUETOOTH_SCANNER, "SCAN INITIATED")

        //BroadcastReceiver

        val status = _bluetoothAdapter?.startDiscovery() ?: false
        return Result.success(status)
    }

    override fun stopScan(): Result<Boolean> {
        Log.d(BLUETOOTH_SCANNER, "SCAN CANCELED")
        val status = _bluetoothAdapter?.cancelDiscovery() ?: false
        return Result.success(status)
    }

    override fun releaseResources() {
        Log.d(BLUETOOTH_SCANNER, "RECEIVER REMOVED")
        try {

        } catch (e: Exception) {
            Log.d(BLUETOOTH_SCANNER, "---------")
        }
    }
}
