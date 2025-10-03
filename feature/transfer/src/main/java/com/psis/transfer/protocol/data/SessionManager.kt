package com.psis.transfer.protocol.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.psis.transfer.protocol.domain.ExchangeProtocol
import com.psis.transfer.protocol.domain.SessionManagerState
import com.psis.transfer.protocol.domain.model.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val exchangeProtocol: ExchangeProtocol,
    private val liftRepository: LiftRepository,
) {
    private var sessionScope: CoroutineScope? = null
    private var socket: BluetoothSocket? = null

    private val _sessionState =
        MutableStateFlow(SessionManagerState.States(SessionManagerState.State.STOPPED))
    val sessionState: StateFlow<SessionManagerState.States> = _sessionState


    fun start(socket: BluetoothSocket) {
        this.socket = socket

        sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO).apply {
            // read
            launchSafely {
                exchangeProtocol.listen(socket).collect { parsed ->
                    liftRepository.updateData(parsed)
                }
            }

            // request
            launchSafely { requestLoop(socket, isActive = isActive) }

            // уведомление о старте
            _sessionState.value = SessionManagerState.States(SessionManagerState.State.STARTED)
        }

    }

    fun stop() {
        Log.d("test", "stop")
        if (sessionScope == null) return

        liftRepository.clear()
        liftRepository.updateData(LiftDataDefaults.getDefault())
        sessionScope?.cancel()
        sessionScope = null

        socket?.closeSafely()
        socket = null

        _sessionState.value = SessionManagerState.States(SessionManagerState.State.STOPPED)
    }

    private suspend fun requestLoop(socket: BluetoothSocket, isActive: Boolean) {
        while (isActive) {
            exchangeProtocol.request(socket, Command.READ_FROM_ADDRESS_0.bytes)
            delay(RETRY_DELAY_MS)
        }
    }

    private fun CoroutineScope.launchSafely(block: suspend CoroutineScope.() -> Unit): Job =
        launch {
            runCatching { block() }
                .onFailure {
                    SessionManagerState.States(
                        SessionManagerState.State.STOPPED,
                        SessionManagerState.State.ERROR
                    )
                }
        }

    private fun BluetoothSocket.closeSafely() {
        try {
            close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket", e)
        }
    }

    fun sendCommand(command: ByteArray) {
        socket?.let { safeSocket ->
            exchangeProtocol.sendCommand(safeSocket, command)
        } ?: Log.w(TAG, "Attempt to send command with no active socket")
    }

    companion object {
        const val RETRY_DELAY_MS = 200L
        private val TAG = SessionManager::class.java.simpleName
    }
}