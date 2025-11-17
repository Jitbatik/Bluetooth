package com.psis.transfer.protocol.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElevatorArchiveBufferRepository @Inject constructor() {

    private val _storage = MutableStateFlow<List<List<Byte>>>(emptyList())
    fun observe() = _storage.asStateFlow()

    /** Добавление нового блока */
    fun putBlock(payload: List<Byte>) {
        _storage.update { it + listOf(payload) } // добавляем в конец
    }

    /** Очистка архива */
    fun clear() {
        _storage.value = emptyList()
    }

    /** Проверка, пуст ли архив */
    fun isEmpty(): Boolean = _storage.value.isEmpty()

    /** Текущее состояние архива (однократный доступ) */
    fun current(): List<List<Byte>> = _storage.value
}