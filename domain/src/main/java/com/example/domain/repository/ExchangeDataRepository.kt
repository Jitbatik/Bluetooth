package com.example.domain.repository

import com.example.domain.model.CharData
import kotlinx.coroutines.flow.Flow

interface ExchangeDataRepository {
    fun observeData(): Flow<List<CharData>>
   // suspend fun sendToStream(value: ByteArray): Result<Boolean>

}