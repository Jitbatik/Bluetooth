package com.example.domain.repository

import com.example.domain.model.CharData
import kotlinx.coroutines.flow.Flow

interface ExchangeDataRepository {
    fun getStateSocket(): Flow<Boolean>
    fun getData(): Flow<List<CharData>>
    suspend fun requestData()
    suspend fun sendToStream(value: ByteArray): Result<Boolean>

}