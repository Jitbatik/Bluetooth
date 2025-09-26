package com.example.bluetooth.data.receivers

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi

class BondingReceiver(
    private val device: BluetoothDevice,
    private val continuation: CancellableContinuation<Boolean>
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return

        val bondedDevice = intent.getDevice() ?: return
        if (bondedDevice.address != device.address) return

        bondedDevice.safeBondState(context)?.let { state ->
            when (state) {
                BluetoothDevice.BOND_BONDED -> finish(context, true)
                BluetoothDevice.BOND_NONE -> finish(context, false)
            }
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun finish(context: Context?, result: Boolean) {
        try {
            context?.unregisterReceiver(this)
        } catch (_: Exception) {}
        continuation.resume(result) {}
    }


    private fun Intent.getDevice(): BluetoothDevice? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
        }

    @SuppressLint("MissingPermission")
    private fun BluetoothDevice.safeBondState(context: Context?): Int? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED
        ) {
            bondState
        } else {
            null
        }
    }

}

