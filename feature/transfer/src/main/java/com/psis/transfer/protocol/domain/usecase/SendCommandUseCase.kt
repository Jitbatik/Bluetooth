package com.psis.transfer.protocol.domain.usecase

import com.psis.transfer.protocol.data.command.CommandRepository
import javax.inject.Inject

class SendCommandUseCase @Inject constructor(
    private val commandRepository: CommandRepository,
) {
    operator fun invoke(command: ByteArray) = commandRepository.enqueueUserCommand(command)
}