package com.psis.transfer.protocol.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElevatorArchiveBufferRepository @Inject constructor() {

    private val _storage = MutableStateFlow<List<ByteArray>>(emptyList())
    val storage: StateFlow<List<ByteArray>> get() = _storage

    /** Добавление нового блока */
    fun putBlock(payload: ByteArray) {
        _storage.update { it + payload } // добавляем в конец
    }

    /** Очистка архива */
    fun clear() {
        _storage.value = emptyList()
    }

    /** Проверка, пуст ли архив */
    fun isEmpty(): Boolean = _storage.value.isEmpty()

    /** Текущее состояние архива (однократный доступ) */
    fun current(): List<ByteArray> = _storage.value
}