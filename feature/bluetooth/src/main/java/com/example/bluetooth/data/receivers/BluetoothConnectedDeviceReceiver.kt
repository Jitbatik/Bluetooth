package com.example.bluetooth.data.receivers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.bluetooth.data.utils.toDomainModel
import com.example.bluetooth.model.BluetoothDevice as DomainBluetoothDevice

class BluetoothConnectedDeviceReceiver(
    private val onDevice: (DomainBluetoothDevice?) -> Unit,
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val device = intent?.getBluetoothDevice() ?: return

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val domainDevice = device.toDomainModel()
                onDevice(domainDevice)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val domainDevice = device.toDomainModel()
                onDevice(domainDevice)
            }
        }
    }

    /**
     * Extension для извлечения BluetoothDevice без SuppressWarnings
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun Intent.getBluetoothDevice(): BluetoothDevice? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        else
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
}