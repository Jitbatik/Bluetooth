package com.psis.transfer.protocol.domain.usecase

import com.psis.transfer.protocol.data.LiftDataDefaults
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmulationLiftScreenUseCase @Inject constructor(
    private val elevatorArchiveBufferRepository: ElevatorStateRepository
) {
    /**
     * Возвращает поток данных лифта,
     * эмулируя поведение экрана лифта (фильтрация нужных байт)
     */
    operator fun invoke() = elevatorArchiveBufferRepository
        .observeElevatorState()
        .filterByHeader()
        .filterRelevantBytes()

    /**
     * Фильтрует заголовок - если это дефолтные данные, то пропуск
     * эмулируя поведение экрана лифта (фильтрация нужных байт)
     */
    private fun Flow<List<Byte>>.filterByHeader(
        expectedHeader: List<Byte> = listOf(0x01, 0x03, 0xF0).map { it.toByte() }
    ): Flow<List<Byte>> = filter { data ->
        if (data == LiftDataDefaults.getDefault()) return@filter true
        if (data.size < expectedHeader.size) return@filter false
        expectedHeader.indices.all { i -> data[i] == expectedHeader[i] }
    }

    /**
     * Берёт только нужную часть данных пакета.
     * Если длина меньше минимальной — возвращает как есть, но это пока что
     */
    private fun Flow<List<Byte>>.filterRelevantBytes(): Flow<List<Byte>> =
        map { data ->
            if (data.size < FILTER_END_INDEX) data
            else data.subList(FILTER_START_INDEX, FILTER_END_INDEX)
        }

    companion object {
        private const val FILTER_START_INDEX = 131
        private const val FILTER_END_INDEX = 211
    }
}