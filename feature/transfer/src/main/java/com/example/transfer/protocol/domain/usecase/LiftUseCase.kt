package com.example.transfer.protocol.domain.usecase

import android.util.Log
import com.example.bluetooth.domain.ConnectRepository
import com.example.transfer.protocol.data.SessionManager
import com.example.transfer.protocol.domain.SessionManagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

// todo при дисконекте чтение сокета отпадает, а не отключается не критично
class LiftUseCase @Inject constructor(
    private val connectRepository: ConnectRepository,
    private val sessionManager: SessionManager,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLiftSession(): Flow<SessionManagerState.States> =
        connectRepository.observeSocket()
            .flatMapLatest { result ->
                val socket = result.getOrNull()
                if (socket != null) {
                    Log.d("test", "111")
                    sessionManager.start(socket)
                } else {
                    sessionManager.stop()
                }
                sessionManager.sessionState
            }
}