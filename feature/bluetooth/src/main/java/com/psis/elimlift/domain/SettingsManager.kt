package com.psis.elimlift.domain

import kotlinx.coroutines.flow.StateFlow

interface SettingsManager {
    fun isEnabledChecked(): StateFlow<Boolean>
    fun saveEnabledChecked(isEnabled: Boolean)
    fun getBluetoothMask(): StateFlow<String>
    fun saveBluetoothMask(mask: String)
}