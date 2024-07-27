package com.example.domain.repository

import com.example.domain.model.Device

interface ConnectRepository  {

    fun connectToDevise(device: Device)
}