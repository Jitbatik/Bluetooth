package com.example.data

import com.example.domain.model.CharData
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ExchangeDataRepositoryImpl @Inject constructor(
    private val firstPatternRepository: FirstPatternRepository,
) : ExchangeDataRepository {

    override fun getStateSocket(): Flow<Boolean> = flow {
        emitAll(firstPatternRepository.getStateSocket())
    }

    override fun getData(): Flow<List<CharData>> = flow {
        emitAll(firstPatternRepository.getData())
    }

    override suspend fun requestData() =
        firstPatternRepository.requestData()


    override suspend fun sendToStream(value: ByteArray): Result<Boolean> {
        return firstPatternRepository.sendToStream(value = value)
    }

}