package com.example.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.BluetoothRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class BluetoothRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): BluetoothRepository {

    private val bluetoothDeviceList = mutableListOf<BluetoothDevice>()
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    // надо дописать проверить поддержку блютуз
    // проверить включен ли он есди нет то включить
    override fun initBluetooth(): Boolean {
        return bluetoothAdapter != null
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
}