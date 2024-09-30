package com.example.data.bluetooth

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
import com.example.data.bluetooth.receivers.BluetoothScanReceiver
import com.example.data.bluetooth.receivers.BluetoothStateReceiver
import com.example.data.bluetooth.receivers.ScanDiscoveryReceiver
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ScannerRepository
import com.example.domain.utils.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private typealias BluetoothDeviceModels = List<BluetoothDevice>


@SuppressLint("MissingPermission")
class ScannerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ScannerRepository {

    // _pairedDevices + _availableDevices ||| single device
    private val _bluetoothDeviceList = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    override val deviceList: StateFlow<List<BluetoothDevice>>
        get() = _bluetoothDeviceList.asStateFlow()


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

    override fun observeScanningState(): Flow<Boolean> = callbackFlow {
        trySend(false)
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        val scanDiscoveryReceiver = ScanDiscoveryReceiver(
            onchange = { isScanning ->
                Log.d(TAG, "Scanning state changed: $isScanning")
                trySend(isScanning).isSuccess
            }
        )

        ContextCompat.registerReceiver(
            context,
            scanDiscoveryReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        awaitClose {
            context.unregisterReceiver(scanDiscoveryReceiver)
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


    private val _pairedDevices = MutableStateFlow<BluetoothDeviceModels>(emptyList())
    private fun findPairedDevices(): Result<Unit> {
        if (!_hasScanPermission) {
            Log.e(TAG, "No Bluetooth scan permission granted.")
            return Result.failure(SecurityException("No Bluetooth scan permission granted"))
        }
        Log.d(TAG, "Find paired devices")
        return try {
            val pairedDevices = _bluetoothAdapter?.bondedDevices ?: emptySet()
            val devicesList = pairedDevices.map { device ->
                BluetoothDevice(
                    name = device.name,
                    address = device.address
                )
            }
            _pairedDevices.value = devicesList
            updateDeviceList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private val _scanReceiver = BluetoothScanReceiver { device ->
        _discoverDevices.update { currentDevices -> currentDevices + device }
        updateDeviceList()
    }
    private val _discoverDevices = MutableStateFlow<BluetoothDeviceModels>(emptyList())
    private fun findDiscoverDevices(): Result<Boolean> {
        if (!_hasScanPermission) {
            Log.e(TAG, "No Bluetooth scan permission granted.")
            return Result.failure(SecurityException("No Bluetooth scan permission granted"))
        }
        if (!_hasLocationPermission) {
            Log.e(TAG, "No Bluetooth Location permission granted.")
            return Result.failure(SecurityException("No Bluetooth Location permission granted"))
        }
        Log.d(TAG, "Find discover devices")
        ContextCompat.registerReceiver(
            context,
            _scanReceiver,
            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND),
            ContextCompat.RECEIVER_EXPORTED
        )

        Log.d(TAG, "SCAN INITIATED")
        val status = _bluetoothAdapter?.startDiscovery() ?: false
        return Result.success(status)
    }

    override fun stopScan(): Result<Boolean> {
        if (!_hasScanPermission)
            return Result.failure(SecurityException("No Bluetooth Location permission granted"))
        Log.d(TAG, "SCAN CANCELED")
        val status = _bluetoothAdapter?.cancelDiscovery() ?: false
        return Result.success(status)
    }


    override fun startScan(): Result<Boolean> {
        _pairedDevices.value = emptyList()
        _discoverDevices.value = emptyList()
        _bluetoothDeviceList.value = emptyList()
        //val pairedResult = findPairedDevices()
//        if (pairedResult.isFailure) {
//            Log.e(
//                TAG,
//                "Failed to find paired devices: ${pairedResult.exceptionOrNull()}"
//            )
//        }

        val discoverResult = findDiscoverDevices()
        if (discoverResult.isFailure) {
            Log.e(
                TAG,
                "Failed to discover devices: ${discoverResult.exceptionOrNull()}"
            )
        }

        val status = discoverResult.getOrElse { false }
        return Result.success(status)
    }

    private fun updateDeviceList() {
        val settingsManager = SettingsManager(context)

        val isFilter = settingsManager.isEnabledChecked()
        val bluetoothMask = settingsManager.getBluetoothMask()

        val combinedDevices = (_pairedDevices.value + _discoverDevices.value)
            .distinctBy { it.address }
            .filter { device ->
                if (isFilter) device.name.startsWith(bluetoothMask, ignoreCase = true) else true
            }
        _bluetoothDeviceList.value = combinedDevices
    }


    override fun releaseResources() {
        Log.d(TAG, "RECEIVER REMOVED")
        try {
            context.unregisterReceiver(_scanReceiver)
        } catch (e: Exception) {
            Log.d(TAG, "---------")
        }
    }

    companion object {
        private val TAG = ScannerRepository::class.java.simpleName
    }
}