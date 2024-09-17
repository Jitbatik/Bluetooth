package com.example.domain.utils

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BLUETOOTH_ENABLED = "bluetooth_enabled"
        private const val KEY_BLUETOOTH_MASK = "bluetooth_mask"
    }

    fun saveEnabledChecked(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_BLUETOOTH_ENABLED, enabled).apply()
    }


    fun isEnabledChecked(): Boolean {
        return preferences.getBoolean(KEY_BLUETOOTH_ENABLED, false)
    }

    fun saveBluetoothMask(mask: String) {
        preferences.edit().putString(KEY_BLUETOOTH_MASK, mask).apply()
    }

    fun getBluetoothMask(): String {
        return preferences.getString(KEY_BLUETOOTH_MASK, "") ?: ""
    }
}