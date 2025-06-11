package com.example.transfer.protocol.domain.model

data class UARTPacket(
    val index: Int,
    val dataBytes: List<Byte>
)
