package com.example.data

import com.example.domain.repository.ExchangeDataRepository
import javax.inject.Inject

class ExchangeDataRepositoryImpl @Inject constructor(
    private val firstPatternRepository: FirstPatternRepository,
) : ExchangeDataRepository {

//    override fun getStateSocket(): Flow<Boolean> = flow {
//        emitAll(firstPatternRepository.getStateSocket())
//    }

    override suspend fun requestData() = firstPatternRepository.requestData()
    //private suspend fun sendToStream2(value: ByteArray) = firstPatternRepository.sendToStream(value = value)
//    override suspend fun sendToStream(value: ByteArray): Result<Boolean> {
//        return firstPatternRepository.sendToStream(value = value)
//    }

}