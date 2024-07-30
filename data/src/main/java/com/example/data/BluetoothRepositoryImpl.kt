package com.example.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.core.content.getSystemService
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.BluetoothRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private typealias BluetoothDeviceModels = List<BluetoothDevice>
private const val BLUETOOTH_SCANNER = "BLUETOOTH_REPOSITORY"

@SuppressLint("MissingPermission")
class BluetoothRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : BluetoothRepository {

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

    override fun findPairedDevices(): Result<Unit> {
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
