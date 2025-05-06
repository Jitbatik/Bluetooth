package com.example.transfer.domain.usecase

import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.model.ByteData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

enum class Command(val bytes: ByteArray) {
    READ_FROM_ADDRESS_0(byteArrayOf(0x01, 0x03, 0x00, 0x00, 0x00, 0x78)),
    EMPTY(byteArrayOf())
}

enum class Type {
    READ,
    NOTHING
}

@Singleton
class ObserveParametersUseCase @Inject constructor(
    private val repository: ProtocolDataRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun execute(typeFlow: StateFlow<Type>): Flow<List<ByteData>> = typeFlow
        .flatMapLatest { type -> repository.observeData(generateCommand(type)) }

    private fun generateCommand(type: Type) = when (type) {
        Type.READ -> Command.READ_FROM_ADDRESS_0.bytes
        Type.NOTHING -> Command.EMPTY.bytes
    }
}


