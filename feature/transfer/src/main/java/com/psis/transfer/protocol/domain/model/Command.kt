package com.psis.transfer.protocol.domain.model

enum class Command(val bytes: ByteArray) {
    READ_FROM_ADDRESS_0(byteArrayOf(0x01, 0x03, 0x00, 0x00, 0x00, 0x78)),
    EMPTY(byteArrayOf())
}

