package com.example.domain.repository

interface ExchangeDataRepository {
    //fun getStateSocket(): Flow<Boolean>
    suspend fun requestData()
   // suspend fun sendToStream(value: ByteArray): Result<Boolean>

}