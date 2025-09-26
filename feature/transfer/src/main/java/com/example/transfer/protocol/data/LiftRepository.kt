package com.example.transfer.protocol.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiftRepository @Inject constructor() {
    private val _dataFlow = MutableStateFlow(LiftDataDefaults.getDefault())

    fun observeLiftData(): Flow<List<Byte>> = _dataFlow
    fun updateData(newData: List<Byte>) {
        _dataFlow.value = newData
    }

    fun clear() {
        _dataFlow.value = emptyList()
    }
}


