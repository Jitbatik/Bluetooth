package com.example.domain.repository


import kotlinx.coroutines.flow.Flow

interface ExchangeDataRepository {
    fun getStateSocket(): Flow<Boolean>
    fun readFromStream(canRead: Boolean): Flow<ByteArray>
    suspend fun sendToStream(value: ByteArray): Result<Boolean>
}