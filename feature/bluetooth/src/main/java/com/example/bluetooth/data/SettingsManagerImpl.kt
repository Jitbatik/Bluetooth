package com.example.bluetooth.data

import android.content.Context
import com.example.bluetooth.domain.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SettingsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsManager {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isEnabledFlow = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    override fun isEnabledChecked(): StateFlow<Boolean> = _isEnabledFlow.asStateFlow()

    override fun saveEnabledChecked(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, isEnabled).apply()
        _isEnabledFlow.value = isEnabled
    }

    private val _bluetoothMaskFlow =
        MutableStateFlow(prefs.getString(KEY_MASK, DEFAULT_MASK) ?: DEFAULT_MASK)

    override fun getBluetoothMask(): StateFlow<String> = _bluetoothMaskFlow.asStateFlow()

    override fun saveBluetoothMask(mask: String) {
        prefs.edit().putString(KEY_MASK, mask).apply()
        _bluetoothMaskFlow.value = mask
    }

    companion object {
        private const val DEFAULT_MASK = "CP"
        private const val PREFS_NAME = "bluetooth_settings"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_MASK = "mask"
    }
}