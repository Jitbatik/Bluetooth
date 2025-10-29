package com.psis.transfer.protocol.data.command

import android.util.Log
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandRepository @Inject constructor(
    private val commandFactory: CommandFactory
) {
    private val priorityCommands = Channel<Command<List<Byte>>>(capacity = 4)
    private val archiveCommands = Channel<Command<List<Byte>>>(capacity = 192)

    var lastSentCommand: Command<List<Byte>>? = null
    private var lastStateIndex: Int? = null

    /**
     * Выдает следующую команду с учётом приоритета и состояния.
     */
    fun getNextCommand(): Command<List<Byte>> {
        val previous = lastSentCommand
        val nextCommand = when {
            previous != null && !previous.isStatusCommand() -> commandFactory.status()
            else -> priorityCommands.tryReceive().getOrNull()
                ?: archiveCommands.tryReceive().getOrNull()
                ?: commandFactory.status()
        }

        lastSentCommand = nextCommand
        return nextCommand
    }
    /**
     * Проверка, является ли команда статусной.
     */
    private fun Command<*>.isStatusCommand(): Boolean =
        bytes == CommandFactory.STATUS.map { it.toByte() }



    /**
     * Добавление команды в очередь
     */
    fun enqueueUserCommand(
        commandBytes: ByteArray,
        respondHeader: List<Byte> = emptyList(),
        handleResponse: suspend (List<Byte>) -> Unit = {}
    ) {
        val command = Command(
            bytes = commandBytes.toList(),
            respondHeader = respondHeader,
            handleResponse = handleResponse
        )
        if (!priorityCommands.trySend(command).isSuccess) Log.w(
            TAG, "⚠️ Очередь пользовательских команд переполнена (макс 4)"
        )
        else Log.d(TAG, "🟢 Добавлена команда: ${commandBytes.joinToString(" ") { "%02X".format(it) }}")
    }

    /**
     * Обновляем очередь архивных команд, если требуется
     */
    fun refreshArchiveQueueIfNeeded(isArchiveEmpty: Boolean, currentStateIndex: Int) {
        if (!isArchiveEmpty && currentStateIndex == lastStateIndex) return

        Log.d(TAG, "🔄 Генерация потока архивных команд (stateChanged=${currentStateIndex != lastStateIndex})")
        clearChannel(archiveCommands)
        repeat(ARCHIVE_BLOCK_COUNT) { index ->
            archiveCommands.trySend(commandFactory.readBlock(index))
        }
        lastStateIndex = currentStateIndex
    }

    /**
     * Утилита для очистки канала (без закрытия)
     */
    private fun <E> clearChannel(channel: Channel<E>) {
        while (channel.tryReceive().isSuccess) Unit
    }

    companion object {
        private const val ARCHIVE_BLOCK_COUNT = 192
        private val TAG = CommandRepository::class.java.simpleName
    }
}