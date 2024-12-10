package com.example.bluetooth.data.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager by lazy { context.getSystemService<BluetoothManager>() }
    val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter
}