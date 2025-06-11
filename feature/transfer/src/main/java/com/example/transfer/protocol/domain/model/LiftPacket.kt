package com.example.transfer.protocol.domain.model

data class LiftPacket(
    val slaveAddress: Int,
    val functionCode: Int,
    val regSize: Int,
    val dataList: List<Byte>,
    val checksum: Int,
)