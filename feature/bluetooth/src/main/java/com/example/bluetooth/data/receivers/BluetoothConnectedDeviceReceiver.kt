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

@Suppress("DEPRECATION")
class BluetoothConnectedDeviceReceiver(
    private val onDevice: (DomainBluetoothDevice?) -> Unit,
) : BroadcastReceiver() {
    private val tag = BluetoothConnectedDeviceReceiver::class.java.simpleName

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        else intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                device?.toDomainModel()?.let(onDevice)
                Log.d(tag, "Connected device: ${device?.name} (${device?.address})")
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                onDevice(null)
                //device?.toDomainModel()?.let(onDevice)
                Log.d(tag, "Disconnected device")
            }
        }
    }

}