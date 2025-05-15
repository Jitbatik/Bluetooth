package com.example.bluetooth.domain

interface SettingsManager {
    fun isEnabledChecked(): Boolean
    fun saveEnabledChecked(isEnabled: Boolean)
    fun getBluetoothMask(): String
    fun saveBluetoothMask(mask: String)
}