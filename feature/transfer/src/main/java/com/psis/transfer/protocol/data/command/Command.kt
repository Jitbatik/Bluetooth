package com.psis.transfer.protocol.data.command

data class Command<T>(
    val bytes: List<Byte>,
    val respondHeader: List<Byte> = emptyList(),
    val handleResponse: suspend (T) -> Unit = {}
)