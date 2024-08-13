package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface ExchangeDataRepository {
    fun readFromStream(canRead: Boolean): Flow<ByteArray>
    suspend fun sendToStream(value: String): Result<Boolean>
}