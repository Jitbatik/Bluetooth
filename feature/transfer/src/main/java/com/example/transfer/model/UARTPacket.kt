package com.example.transfer.model

data class UARTPacket(
    val index: Int,
    val dataBytes: List<Byte>
)
