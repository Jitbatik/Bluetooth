package com.example.data

import com.example.domain.model.CharData
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExchangeDataRepositoryImpl @Inject constructor(
    private val protocolDataRepository: ProtocolDataRepository,
) : ExchangeDataRepository {

    override fun observeData(): Flow<List<CharData>> = protocolDataRepository.observeBluetoothDataFlow()
    //private suspend fun sendToStream2(value: ByteArray) = firstPatternRepository.sendToStream(value = value)
//    override suspend fun sendToStream(value: ByteArray): Result<Boolean> {
//        return firstPatternRepository.sendToStream(value = value)
//    }

}