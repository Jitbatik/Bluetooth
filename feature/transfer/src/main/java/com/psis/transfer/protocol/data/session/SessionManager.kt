package com.psis.transfer.protocol.data.session

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.psis.transfer.protocol.data.command.Command
import com.psis.transfer.protocol.data.command.CommandFactory
import com.psis.transfer.protocol.data.command.CommandRepository
import com.psis.transfer.protocol.data.repository.ElevatorArchiveBufferRepository
import com.psis.transfer.protocol.data.repository.ElevatorStateRepository
import com.psis.transfer.protocol.domain.ExchangeProtocol
import com.psis.transfer.protocol.domain.SessionState
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException


/**
 * Менеджер управления Bluetooth-сессией
 */
@Singleton
class SessionManager @Inject constructor(
    private val exchangeProtocol: ExchangeProtocol,
    private val stateRepository: ElevatorStateRepository,
    private val archiveRepository: ElevatorArchiveBufferRepository,
    private val commandRepository: CommandRepository,
    private val commandFactory: CommandFactory,
) {
    private val sessionMutex = Mutex()
    private var session: SessionScope? = null

    private val _state = MutableStateFlow(SessionState.STOPPED)
    val state: StateFlow<SessionState> = _state

    private val responseAwaiter = ResponseAwaiter()

    /**
     * Инициализация и запуск новой сессии.
     */
    suspend fun start(socket: BluetoothSocket) = sessionMutex.withLock {
        stopUnlocked()

        val newSession = SessionScope(socket, Job())
        session = newSession

        newSession.scope.launchSafely("incoming") {
            observeIncomingPackets(newSession)
        }
        newSession.scope.launchSafely("sender") {
            processSendCommands(newSession)
        }

        _state.value = SessionState.STARTED
        Log.i(TAG, "🚀 Сессия запущена")
    }

    /**
     * Безопасная остановка сессии и очистка состояния.
     */
    suspend fun stop() = sessionMutex.withLock {
        stopUnlocked()
    }

    private fun stopUnlocked() {
        session?.close()
        session = null

        stateRepository.clear()
        archiveRepository.clear()
        responseAwaiter.cancel()

        _state.value = SessionState.STOPPED
        Log.i(TAG, "🛑 Сессия остановлена")
    }


    /**
     * Чтение из сокета и запись в репозитории
     */
    private suspend fun observeIncomingPackets(local: SessionScope) {
        exchangeProtocol.listen(local.socket).collect { packet ->
            handleIncomingPacket(packet)
        }
    }

    private suspend fun handleIncomingPacket(packet: List<Byte>) {
        val lastCommand = commandRepository.lastSentCommand ?: return

        if (commandFactory.hasHeader(packet, lastCommand.respondHeader)) {
            lastCommand.handleResponse(packet)
            responseAwaiter.complete()
        }
    }

    /**
     * Основной цикл отправки команд
     */
    private suspend fun processSendCommands(local: SessionScope) {
        val socket = local.socket

        while (local.scope.isActive) {

            // наполнение очереди
            commandRepository.refreshArchiveQueueIfNeeded(
                currentStateIndex = stateRepository.getCurrentBufferIndex()
            )

            // отправка
            val command = commandRepository.getNextCommand()
            val ok = sendWithRetry(socket, command)

            if (!ok) {
                Log.e(TAG, "❌ Нет ответа после $MAX_RETRY_COUNT попыток")
            }

            delay(RETRY_DELAY_MS)
        }
    }


    /**
     * Ожидание ответа от устройства
     */
    private suspend fun sendWithRetry(
        socket: BluetoothSocket,
        command: Command<List<Byte>>
    ): Boolean {
        for (attempt in 1..MAX_RETRY_COUNT) {
            Log.d(
                TAG,
                "➡️ Отправка #$attempt: ${command.bytes.joinToString(" ") { "%02X".format(it) }} " +
                        "(header=${command.respondHeader.joinToString(" ") { "%02X".format(it) }})"
            )
            if (!currentCoroutineContext().isActive) break
            exchangeProtocol.sendCommand(socket, command.bytes.toByteArray())
            if (command.respondHeader.isEmpty()) return true
            if (responseAwaiter.await(RESPONSE_TIMEOUT_MS)) return true

            Log.w(TAG, "⏳ Нет ответа в течение ${RESPONSE_TIMEOUT_MS}мс, повтор...")
        }
        return false
    }


    /**
     * Безопасный запуск корутины
     */
    private fun CoroutineScope.launchSafely(
        name: String,
        block: suspend () -> Unit
    ): Job = launch(CoroutineName(name)) {
        try {
            block()
        } catch (_: CancellationException) {
            Log.d(TAG, "🟡 Короутина [$name] отменена (нормальное завершение)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка в корутине [$name]", e)
            _state.value = SessionState.ERROR
        }
    }

    companion object {
        private const val RETRY_DELAY_MS = 150L
        private const val RESPONSE_TIMEOUT_MS = 1000L
        private const val MAX_RETRY_COUNT = 3
        private val TAG = SessionManager::class.java.simpleName
    }
}