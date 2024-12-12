package com.example.transfer

data class UARTPacket(
    val index: Int,
    val dataBytes: List<Byte>
)
