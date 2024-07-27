package com.example.domain.repository

import androidx.lifecycle.LiveData
import com.example.domain.model.Device

interface BluetoothDeviseRepository {

    fun getSavedBluetoothDevise() : LiveData<List<Device>>

    fun scanNewDevise() : LiveData<List<Device>>
}