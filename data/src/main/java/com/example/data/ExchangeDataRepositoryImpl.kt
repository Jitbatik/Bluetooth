package com.example.data

import com.example.domain.model.CharData
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExchangeDataRepositoryImpl @Inject constructor(
    private val firstPatternRepository: FirstPatternRepository,
) : ExchangeDataRepository {

    //    private fun closeStreams() {
//        try {
//            getInputStream()?.close()
//            getOutputStream()?.close()
//            Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "CLOSE STREAMS")
//        } catch (e: IOException) {
//            Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error closing streams", e)
//        }
//    }

    override suspend fun requestData(): Flow<List<CharData>> =
        firstPatternRepository.requestData()

}