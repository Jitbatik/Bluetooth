package com.psis.transfer.protocol.domain

import com.psis.transfer.protocol.domain.model.ByteData
import com.psis.transfer.protocol.domain.model.ControllerConfig
import kotlinx.coroutines.flow.Flow

interface ProtocolDataRepository {
    fun observeData(command: ByteArray): Flow<List<ByteData>>
    fun observeControllerConfig(): Flow<ControllerConfig>
    fun sendToStream(value: ByteArray)

    //TODO: Убрать после тестов
    fun getAnswerTest(): Flow<String>
}