package com.psis.transfer.protocol.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.psis.transfer.protocol.domain.ExchangeProtocol
import com.psis.transfer.protocol.domain.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Менеджер управления Bluetooth-сессией
 */
@Singleton
class SessionManager @Inject constructor(
    private val exchangeProtocol: ExchangeProtocol,
    private val liftRepository: LiftRepository,
) {
    @Volatile
    private var session: SessionScope? = null

    private val _state = MutableStateFlow(SessionState.STOPPED)
    val state: StateFlow<SessionState> = _state


    /**
     * Инициализация и запуск новой сессии.
     */
    fun start(
        socket: BluetoothSocket,
        baseCommand: StateFlow<ByteArray>,
    ) = synchronized(this) {
        stop()

        val channel = Channel<ByteArray>(Channel.BUFFERED)
        val sessionScope = SessionScope(socket, channel, Job())
        session = sessionScope

        // read
        sessionScope.scope.launchSafely {
            listen(socket)
        }

        // request
        sessionScope.scope.launchSafely {
            sendLoop(socket, channel, baseCommand)
        }

        _state.value = SessionState.STARTED
    }

    /**
     * Безопасная остановка сессии и очистка состояния.
     */
    fun stop() {
        session?.close()
        session = null
        liftRepository.clear()
        liftRepository.updateData(LiftDataDefaults.getDefault())

        _state.value = SessionState.STOPPED
    }


    /**
     * Добавление команды в очередь
     */
    fun sendCommand(command: ByteArray) {
        val ctx = session ?: run {
            Log.w(TAG, "⚠️ Нет активной сессии — команда проигнорирована")
            return
        }

        if (ctx.channel.trySend(command).isSuccess) {
            Log.d(TAG, "🟢 Команда добавлена в очередь: ${command.toHexString()}")
        } else {
            Log.w(TAG, "⚠️ Очередь переполнена — команда отброшена")
        }
    }

    /**
     * Чтение из сокета и запись в репозиторий
     */
    private suspend fun listen(socket: BluetoothSocket) {
        exchangeProtocol.listen(socket).collect { parsed ->
            Log.d(TAG, "📥 Ответ: ${parsed.toByteArray().toHexString()}")
            liftRepository.updateData(parsed)
        }
    }

    /**
     * Основной цикл отправки команд
     */
    private suspend fun sendLoop(
        socket: BluetoothSocket,
        channel: Channel<ByteArray>,
        baseCommand: StateFlow<ByteArray>
    ) {
        while (currentCoroutineContext().isActive) {
            val cmd = withTimeoutOrNull(RETRY_DELAY_MS) { channel.receive() } ?: baseCommand.value
            Log.d(TAG, "➡️ Отправка: ${cmd.toHexString()}")
            exchangeProtocol.sendCommand(socket, cmd)
        }
    }


    /**
     * Безопасный запуск корутины
     */
    private fun CoroutineScope.launchSafely(block: suspend CoroutineScope.() -> Unit): Job =
        launch {
            runCatching { block() }
                .onFailure {
                    Log.e(TAG, "❌ Ошибка в корутине", it)
                    SessionState.ERROR
                }
        }

    companion object {
        const val RETRY_DELAY_MS = 200L
        private val TAG = SessionManager::class.java.simpleName
    }
}

/**
 * Контейнер, инкапсулирующий состояние активной Bluetooth-сессии
 */
private class SessionScope(
    private val socket: BluetoothSocket,
    val channel: Channel<ByteArray>,
    parentJob: Job
) : Closeable {

    val scope = CoroutineScope(SupervisorJob(parentJob) + Dispatchers.IO)

    override fun close() {
        runCatching {
            channel.close()
            socket.close()
        }.onFailure {
            Log.w(TAG, "Ошибка при закрытии сессии", it)
        }
        scope.cancel()
    }

    companion object {
        private val TAG = SessionScope::class.java.simpleName
    }
}

/**
 * Утилита форматирования ByteArray для логов.
 */
private fun ByteArray.toHexString(): String =
    joinToString(" ") { "%02X".format(it) }