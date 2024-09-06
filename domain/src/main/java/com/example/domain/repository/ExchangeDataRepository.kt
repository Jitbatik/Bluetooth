package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface ExchangeDataRepository {
    fun getStateSocket(): Flow<Boolean>
    suspend fun requestData()
    suspend fun sendToStream(value: ByteArray): Result<Boolean>
}