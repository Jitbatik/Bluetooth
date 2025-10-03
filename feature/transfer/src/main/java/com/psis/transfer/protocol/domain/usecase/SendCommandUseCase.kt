package com.psis.transfer.protocol.domain.usecase

import com.psis.transfer.protocol.data.SessionManager
import javax.inject.Inject

class SendCommandUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(command: ByteArray) = sessionManager.sendCommand(command)
}