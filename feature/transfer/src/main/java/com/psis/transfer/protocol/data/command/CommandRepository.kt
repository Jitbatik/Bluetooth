package com.psis.transfer.protocol.data.command

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandRepository @Inject constructor(
    private val commandFactory: CommandFactory
) {
    private val priorityCommands = Channel<Command<List<Byte>>>(capacity = 4)
    private val archiveCommands = Channel<Command<List<Byte>>>(capacity = ARCHIVE_COMMAND_COUNT)
    private val archiveInitCommands = Channel<Command<List<Byte>>>(capacity = ARCHIVE_COMMAND_COUNT)

    var lastSentCommand: Command<List<Byte>>? = null
    private val lock = Mutex()
    private var lastStateIndex: Int = -1
    private var connectionActive = false

    fun getNextCommand(): Command<List<Byte>> {
        val previous = lastSentCommand

        val next =
            when {
                // если предыдущая НЕ статус — следующий статус
                previous?.isStatusCommand() == false -> commandFactory.status()

                else ->
                    priorityCommands.tryReceive().getOrNull()
                        ?: archiveCommands.tryReceive().getOrNull()
                        ?: archiveInitCommands.tryReceive().getOrNull()
                        ?: commandFactory.status()
            }

        lastSentCommand = next
        return next
    }

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
        if (!priorityCommands.trySend(command).isSuccess)
            Log.w(TAG, "⚠️ Очередь пользовательских команд переполнена (макс 4)")
        else
            Log.d(
                TAG, "🟢 Добавлена: ${commandBytes.joinToString(" ") { "%02X".format(it) }}"
            )
    }

    /**
     * 🔧 Потокобезопасная проверка необходимости перегенерации архивных команд.
     */
    suspend fun refreshArchiveQueueIfNeeded(currentStateIndex: Int?) {
        lock.withLock {
            val last = lastStateIndex
            Log.d("TAG", "currentStateIndex = $currentStateIndex last = $last")

            if (!connectionActive && currentStateIndex != null) {
                Log.d(TAG, "🔄 Новое подключение — сброс архива")
                resetArchiveState()
                connectionActive = true
            }

            if (currentStateIndex == null) {
                connectionActive = false
                return
            }
            if (currentStateIndex == last) return

            Log.d(TAG, "✅ Изменился currentStateIndex = $currentStateIndex")

            if (last == -1) fillArchiveInitCommands(currentStateIndex = currentStateIndex)
            else regenerateArchiveSequence(lastIndex = last, current = currentStateIndex)


            lastStateIndex = currentStateIndex
        }
    }

    private fun resetArchiveState() {
        clearChannel(archiveCommands)
        clearChannel(archiveInitCommands)
        lastStateIndex = -1
    }

    private fun fillArchiveInitCommands(
        currentStateIndex: Int,
        totalBlocks: Int = ARCHIVE_INIT_BLOCK_COUNT
    ) {
        Log.d(
            TAG,
            "🆕 Начальная загрузка архива: формируем полный цикл от $currentStateIndex → 0 " +
                    "и $totalBlocks → $currentStateIndex"
        )

        // Очищаем каналы перед заполнением
        clearChannel(archiveInitCommands)

        // От currentStateIndex до 0
        for (i in currentStateIndex downTo 0) {
            archiveInitCommands.trySend(commandFactory.readBlock(i))
        }
        // От totalBlocks - 1 до currentStateIndex + 1
        for (i in totalBlocks - 1 downTo currentStateIndex + 1) {
            archiveInitCommands.trySend(commandFactory.readBlock(i))
        }
    }

    private fun regenerateArchiveSequence(
        lastIndex: Int,
        current: Int,
        total: Int = ARCHIVE_INIT_BLOCK_COUNT
    ) {
        Log.d(TAG, "🔁 Инкрементальная генерация архива")

        var idx = current
        while (true) {
            archiveCommands.trySend(commandFactory.readBlock(idx))
            if (idx == lastIndex) break
            idx = (idx - 1 + total) % total
        }
    }

    private fun <E> clearChannel(channel: Channel<E>) {
        while (channel.tryReceive().isSuccess) Unit
    }

    private fun Command<*>.isStatusCommand(): Boolean =
        bytes == CommandFactory.STATUS.map { it.toByte() }


    companion object {
        private const val ARCHIVE_INIT_BLOCK_COUNT = 192
        private const val ARCHIVE_COMMAND_COUNT = 384
        private val TAG = CommandRepository::class.java.simpleName
    }
}
