package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.getSystemService
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.BluetoothRepository
import com.example.domain.repository.PermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


private const val BLUETOOTH_SCANNER = "ANDROID_BLUETOOTH_SCANNER"

@SuppressLint("MissingPermission")
class BluetoothRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
): BluetoothRepository, PermissionRepository  {

    private val bluetoothDeviceList = mutableListOf<BluetoothDevice>()

    /**
    * Получаем блютуз адаптер
     */
    private val _bluetoothManager by lazy { context.getSystemService<BluetoothManager>() }
    private val _bluetoothAdapter: BluetoothAdapter?
        get() = _bluetoothManager?.adapter


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

    /**
    * Пока просто заглушка проверок
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    override fun preparationBluetooth(): Boolean {
        if (!_hasScanPermission) {
            Log.e(BLUETOOTH_SCANNER, "NO scan permission")
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                REQUEST_CODE_SCAN_PERMISSION
            )
            return false // буду писать исключения
        }
        if (!_hasLocationPermission) {
            Log.e(BLUETOOTH_SCANNER, "NO location permission")
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION_PERMISSION
            )
            return false
        }
        val bluetoothAdapter = _bluetoothAdapter
        if (bluetoothAdapter == null) {
            Log.e(BLUETOOTH_SCANNER, "NO SUPPORT bluetooth")
            return false
        }

        if (bluetoothAdapter.isEnabled) {
            Log.e(BLUETOOTH_SCANNER, "NO enable bluetooth")
            // надо АКТИВИТИ для включения
            return false
        }
        return true
    }

    override fun getPairedDevice(): Flow<List<BluetoothDevice>> {
        return flow {
            // Emit the current list of devices
            emit(bluetoothDeviceList)
        }.flowOn(Dispatchers.IO) // Ensure it's running on the IO dispatcher
    }

    override fun getScannedDevice(): Flow<List<BluetoothDevice>> {
        return flow {
            // Simulate scanning and adding new devices
            bluetoothDeviceList.addAll(listOf(
                BluetoothDevice("Device 3", "00:11:22:33:44:55"),
                BluetoothDevice("Device 4", "66:77:88:99:AA:BB")
            ))
            emit(bluetoothDeviceList)
        }.flowOn(Dispatchers.IO) // Ensure it's running on the IO dispatcher
    }

    override fun stopScan(): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun releaseResources() {
        TODO("Not yet implemented")
    }

    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        if (permissions.isNotEmpty()) {
            // Надо АКТИВИТИ
        }
    }

    companion object {
        private const val REQUEST_CODE_SCAN_PERMISSION = 1001
        private const val REQUEST_CODE_LOCATION_PERMISSION = 1002
    }
}