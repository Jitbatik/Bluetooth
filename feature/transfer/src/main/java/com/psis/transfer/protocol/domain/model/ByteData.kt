package com.psis.transfer.protocol.domain.model

data class ByteData(
    val byte: Byte,
    val colorByte: Int = 0,
    val backgroundByte: Int = 15,
)
