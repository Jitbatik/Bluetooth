package com.example.bluetooth.data.receivers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi

class BondingReceiver(
    private val device: BluetoothDevice,
    private val continuation: CancellableContinuation<Boolean>
) : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val bondedDevice: BluetoothDevice? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE,
                    BluetoothDevice::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
            }

        if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED && bondedDevice?.address == device.address) {
            bondedDevice?.let {
                when (it.bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        context?.unregisterReceiver(this)
                        continuation.resume(true) {}
                    }

                    BluetoothDevice.BOND_NONE -> {
                        context?.unregisterReceiver(this)
                        continuation.resume(false) {}
                    }
                }
            }
        }
    }
}