package com.psis.transfer.protocol.data.command

import com.psis.transfer.protocol.data.repository.ElevatorArchiveBufferRepository
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandFactory @Inject constructor(
    private val stateRepository: ElevatorStateRepository,
    private val archiveRepository: ElevatorArchiveBufferRepository,
) {
    private object Headers {
        val STATE = listOf(0x01, 0x03, 0xF0).map(Int::toByte)
        val ARCHIVE = listOf(0x01, 0x03, 0x40).map(Int::toByte)
    }

    /**
     * Проверяет, начинается ли пакет с заданного заголовка.
     */
    fun hasHeader(packet: List<Byte>, expected: List<Byte>): Boolean =
        packet.size >= expected.size && expected.indices.all { packet[it] == expected[it] }


    fun status(): Command<List<Byte>> = Command(
        bytes = listOf(0x01, 0x03, 0x00, 0x00, 0x00, 0x78),
        respondHeader = Headers.STATE,
        handleResponse = { stateRepository.update(it) }
    )

    fun readBlock(
        index: Int,
        baseAddress: Int = 0x0200,
        step: Int = 0x20,
        length: Int = 0x20,
    ): Command<List<Byte>> {
        val address = baseAddress + index * step
        val high = ((address shr 8) and 0xFF).toByte()
        val low = (address and 0xFF).toByte()

        return Command(
            bytes = listOf(0x01, 0x03, high, low, 0x00, length.toByte()),
            respondHeader = Headers.ARCHIVE,
            handleResponse = { archiveRepository.putBlock(it.toByteArray()) }
        )
    }

    companion object {
        val STATUS = listOf(0x01, 0x03, 0x00, 0x00, 0x00, 0x78)
    }
}