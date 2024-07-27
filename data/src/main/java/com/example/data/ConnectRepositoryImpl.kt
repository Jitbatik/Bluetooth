package com.example.data

import com.example.domain.repository.ConnectRepository
import javax.inject.Inject

class ConnectRepositoryImpl @Inject constructor(): ConnectRepository {

    //подключение к выбранному девайсу
    override fun connectToDevise(deviceName: String) {

    }
}