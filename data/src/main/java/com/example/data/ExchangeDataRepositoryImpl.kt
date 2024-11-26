package com.example.data

import com.example.domain.model.CharData
import com.example.domain.model.ControllerConfig
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExchangeDataRepositoryImpl @Inject constructor(
    //private val protocolDataRepository: ProtocolUARTDataRepository,
    private val protocolDataRepository: ProtocolModbusDataRepository,
) : ExchangeDataRepository {

    override fun observeData(): Flow<List<CharData>> =
        protocolDataRepository.observeBluetoothDataFlow()

    override fun observeControllerConfig(): Flow<ControllerConfig> =
        protocolDataRepository.observeControllerConfigFlow()

    override suspend fun sendToStream(value: ByteArray) =
        protocolDataRepository.sendToStream(value = value)

    //TODO: Убрать после тестов
    override fun getAnswerTest(): Flow<String> = protocolDataRepository.getAnswerTest()
}