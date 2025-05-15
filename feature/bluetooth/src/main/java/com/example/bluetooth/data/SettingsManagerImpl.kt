package com.example.bluetooth.data

import android.content.Context
import com.example.bluetooth.domain.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsManager {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isEnabledChecked(): Boolean {
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    override fun saveEnabledChecked(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, isEnabled).apply()
    }

    override fun getBluetoothMask(): String {
        return prefs.getString(KEY_MASK, "") ?: ""
    }

    override fun saveBluetoothMask(mask: String) {
        prefs.edit().putString(KEY_MASK, mask).apply()
    }

    companion object {
        private const val PREFS_NAME = "bluetooth_settings"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_MASK = "mask"
    }
}