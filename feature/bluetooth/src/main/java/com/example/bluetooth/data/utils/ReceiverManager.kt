package com.example.bluetooth.data.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Утилита для безопасной работы с BroadcastReceiver
 */
class ReceiverManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val active = mutableSetOf<BroadcastReceiver>()

    fun register(receiver: BroadcastReceiver, filter: IntentFilter) {
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        active += receiver
    }

    fun unregister(receiver: BroadcastReceiver) {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
        active -= receiver
    }

    fun clear() {
        active.forEach { unregister(it) }
        active.clear()
    }
}