package com.example.domain.repository

import com.example.domain.model.CharData
import com.example.domain.model.Command
import com.example.domain.model.ControllerConfig
import kotlinx.coroutines.flow.Flow

interface ExchangeDataRepository {
    fun observeData(): Flow<List<CharData>>
    fun observeControllerConfig(): Flow<ControllerConfig>
    suspend fun sendToStream(value: Command)

    //TODO: Убрать после тестов
    fun getAnswerTest(): Flow<String>
}