package com.example.transfer.data

import android.util.Log
import com.example.bluetooth.data.DataStreamRepository
import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.model.CharData
import com.example.transfer.model.ControllerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ProtocolLiftDataRepository @Inject constructor(
    private val dataStreamRepository: DataStreamRepository,
): ProtocolDataRepository {
    private val tag = ProtocolLiftDataRepository::class.java.simpleName
    //TODO: Убрать после тестов
    private val _answerFlowTest = MutableStateFlow("Command send")
    override fun getAnswerTest(): Flow<String> = _answerFlowTest
    //

    override fun observeData(): Flow<List<CharData>> = flow {
        Log.d(tag, "Initializing Bluetooth data flow")
        dataStreamRepository.observeSocketStream()
            .collect { byteArray ->
                Log.d(tag, "Data: ${byteArray.joinToString(" ")}")
                emit(emptyList<CharData>())
            }
    }.flowOn(Dispatchers.IO)

    override fun observeControllerConfig(): Flow<ControllerConfig> {
        TODO("Not yet implemented")
    }

    override fun sendToStream(value: ByteArray) {
        TODO("Not yet implemented")
    }


}