package com.example.transfer.protocol.domain.usecase

import com.example.transfer.protocol.data.SessionManager
import javax.inject.Inject

class SendCommandUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(command: ByteArray) = sessionManager.sendCommand(command)
}