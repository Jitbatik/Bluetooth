package com.example.transfer.domain.parameters.usecase

import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.domain.parameters.Command
import com.example.transfer.domain.parameters.Type
import com.example.transfer.model.ByteData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

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


