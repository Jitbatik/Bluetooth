package com.example.domain.repository

import com.example.domain.model.CharData
import kotlinx.coroutines.flow.Flow

interface ExchangeDataRepository {
    //fun getStateSocket(): Flow<Boolean>
    suspend fun requestData(): Flow<List<CharData>>
}