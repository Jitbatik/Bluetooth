package com.example.transfer.domain

import com.example.transfer.model.CharData
import com.example.transfer.model.ControllerConfig
import kotlinx.coroutines.flow.Flow

interface ProtocolDataRepository {
    fun observeData(): Flow<List<CharData>>
    fun observeControllerConfig(): Flow<ControllerConfig>
    fun sendToStream(value: ByteArray)

    //TODO: Убрать после тестов
    fun getAnswerTest(): Flow<String>
}