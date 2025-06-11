package com.example.transfer.protocol.domain.model

data class PultPacket(
    val slaveAddress: Int,
    val functionCode: Int,
    val startRegisterRead: Int,
    val quantityRegisterRead: Int,
    val startRegisterWrite: Int,
    val quantityRegisterWrite: Int,
    val counter: Int,
    val dataList: List<Byte>,
    val checksum: Int,
)
