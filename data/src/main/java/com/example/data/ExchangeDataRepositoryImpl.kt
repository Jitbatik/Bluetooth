package com.example.data

import com.example.domain.model.CharData
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExchangeDataRepositoryImpl @Inject constructor(
    private val firstPatternRepository: FirstPatternRepository,
) : ExchangeDataRepository {

    //    override fun getStateSocket(): Flow<Boolean> = flow {
//        emit(false)
//        //emitAll(firstPatternRepository.getStateSocket())
//    }

    override suspend fun requestData(): Flow<List<CharData>> =
        firstPatternRepository.requestData()
}