package com.psis.elimlift.data.receivers

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScanDiscoveryReceiver(
    private val onchange: (Boolean) -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == null) return
        when (intent.action) {
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> onchange(true)
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> onchange(false)
        }
    }
}