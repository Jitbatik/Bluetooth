package com.example.bluetooth.data.receivers

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice as AndroidBluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.bluetooth.data.utils.toDomainModel
import com.example.bluetooth.model.BluetoothDevice

@Suppress("DEPRECATION")
class BluetoothScanReceiver(
    private val onDevice: (BluetoothDevice) -> Unit,
) : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(
                AndroidBluetoothDevice.EXTRA_DEVICE,
                AndroidBluetoothDevice::class.java
            )
        else intent.getParcelableExtra(AndroidBluetoothDevice.EXTRA_DEVICE)

        val hasPermission = context?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PermissionChecker.PERMISSION_GRANTED
            else true
        } ?: false

        when {
            hasPermission && intent.action == AndroidBluetoothDevice.ACTION_FOUND -> {
                val rssi = intent.getShortExtra(AndroidBluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                device?.toDomainModel(rssi)?.let(onDevice)
                Log.d(TAG, "Device found: ${device?.name} (${device?.address}) RSSI: $rssi")
            }
            !hasPermission -> Log.d(TAG, "DON'T HAVE PERMISSION")
        }

    }

    companion object {
        private val TAG = BluetoothScanReceiver::class.java.simpleName
    }
}