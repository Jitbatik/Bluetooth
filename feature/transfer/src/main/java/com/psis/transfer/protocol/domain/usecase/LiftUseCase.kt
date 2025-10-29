package com.psis.transfer.protocol.domain.usecase

import com.psis.elimlift.domain.ConnectRepository
import com.psis.transfer.protocol.data.session.SessionManager
import com.psis.transfer.protocol.domain.SessionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class LiftUseCase @Inject constructor(
    private val connectRepository: ConnectRepository,
    private val sessionManager: SessionManager,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLiftSession(): Flow<SessionState> =
        connectRepository.observeSocket()
            .flatMapLatest { result ->
                val socket = result.getOrNull()
                if (socket != null) {
                    sessionManager.start(socket)
                } else {
                    sessionManager.stop()
                }
                sessionManager.state
            }
}