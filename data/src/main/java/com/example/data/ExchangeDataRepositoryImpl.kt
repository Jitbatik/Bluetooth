package com.example.data

import com.example.domain.model.CharData
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExchangeDataRepositoryImpl @Inject constructor(
    //private val protocolDataRepository: ProtocolUARTDataRepository,
    private val protocolDataRepository: ProtocolModbusDataRepository,
) : ExchangeDataRepository {

    override fun observeData(): Flow<List<CharData>> =
        protocolDataRepository.observeBluetoothDataFlow()

    override suspend fun sendToStream(value: ByteArray) =
        protocolDataRepository.sendToStream(value = value)
}