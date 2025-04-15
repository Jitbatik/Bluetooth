package com.example.transfer.domain.usecase

import android.util.Log
import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.model.ByteData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
    fun execute(typeFlow: StateFlow<Type>): Flow<List<ByteData>> {
        return typeFlow
            .flatMapLatest { type ->
                val command = generateCommand(type)
                Log.d(
                    "ObserveParametersUseCase",
                    "New command generated: ${command.joinToString()}"
                )
                if (command.isEmpty()) {
                    flow { awaitCancellation() }
                } else {
                    repository.observeData(command)
                }
            }
    }

    private fun generateCommand(type: Type) = when (type) {
        Type.READ -> Command.READ_FROM_ADDRESS_0.bytes
        Type.NOTHING -> Command.EMPTY.bytes
    }

}


