package com.example.bluetooth.data.receivers

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothStateReceiver(
    private val isBtOn: (Boolean) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
        when {
            intent.action == BluetoothAdapter.ACTION_STATE_CHANGED ->
                isBtOn.invoke(btState == BluetoothAdapter.STATE_ON)
        }
    }
}