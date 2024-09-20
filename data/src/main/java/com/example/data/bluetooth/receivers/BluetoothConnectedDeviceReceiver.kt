package com.example.data.bluetooth.receivers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.bluetooth.mapper.toDomainModel

import com.example.domain.model.BluetoothDevice as DomainBluetoothDevice

@Suppress("DEPRECATION")
class BluetoothConnectedDeviceReceiver(
    private val onDevice: (DomainBluetoothDevice?) -> Unit,
) : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        else intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                device?.toDomainModel()?.let(onDevice)
                Log.d(TAG, "Connected device: ${device?.name} (${device?.address})")
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                onDevice(null)
                //device?.toDomainModel()?.let(onDevice)
                Log.d(TAG, "Disconnected device")
            }
        }
    }

    companion object {
        private val TAG = BluetoothConnectedDeviceReceiver::class.java.simpleName
    }
}