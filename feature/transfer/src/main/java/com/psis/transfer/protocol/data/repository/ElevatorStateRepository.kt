package com.psis.transfer.protocol.data.repository

import com.psis.transfer.protocol.data.LiftDataDefaults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElevatorStateRepository @Inject constructor() {
    private val _dataFlow = MutableStateFlow(LiftDataDefaults.getDefault())
    fun observeElevatorState(): Flow<List<Byte>> = _dataFlow
    fun update(newData: List<Byte>) {
        _dataFlow.value = newData
    }

    fun getCurrentBufferIndex(): Int {
        val data = _dataFlow.value
        return if (data.size > 91) {
            // приведение к unsigned-like (чтобы избежать отрицательных значений)
            data[91].toInt() and 0xFF
        } else {
            0
        }
    }

    fun clear() {
        _dataFlow.value = emptyList()
    }
}