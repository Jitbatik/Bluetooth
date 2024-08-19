package com.example.domain.repository


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ExchangeDataRepository {
    val data: StateFlow<Boolean>
    fun readFromStream(canRead: Boolean): Flow<ByteArray>
    suspend fun sendToStream(value: String): Result<Boolean>
}