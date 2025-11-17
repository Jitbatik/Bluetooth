package com.psis.transfer.protocol.data.command

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
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
    private val lastStateIndex = AtomicInteger(-1)
    private val archiveLock = Mutex() // 👈 защищает от параллельных перегенераций

    fun getNextCommand(): Command<List<Byte>> {
//        val previous = lastSentCommand
        val next = when {
            lastSentCommand?.isStatusCommand() == false -> commandFactory.status()

            else -> priorityCommands.tryReceive().getOrNull()
                ?: archiveCommands.tryReceive().getOrNull()
                ?: archiveInitCommands.tryReceive().getOrNull()
                ?: commandFactory.status()
        }
        lastSentCommand = next
        return next
    }

    private fun Command<*>.isStatusCommand(): Boolean =
        bytes == CommandFactory.STATUS.map { it.toByte() }

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
                TAG,"🟢 Добавлена: ${commandBytes.joinToString(" ") { "%02X".format(it) }}"
            )
    }

    /**
     * 🔧 Потокобезопасная проверка необходимости перегенерации архивных команд.
     */
    suspend fun refreshArchiveQueueIfNeeded(currentStateIndex: Int?) {
        archiveLock.withLock {
            val last = lastStateIndex.get()
            Log.d("TAG", "currentStateIndex = $currentStateIndex last = $last")
            if (currentStateIndex == null) return
            if (currentStateIndex == last) return

            Log.d(TAG, "✅ Изменился currentStateIndex = $currentStateIndex")
            regenerateArchiveCommands(
                lastIndex = last,
                currentStateIndex = currentStateIndex,
                totalBlocks = ARCHIVE_INIT_BLOCK_COUNT
            )
            lastStateIndex.set(currentStateIndex)
        }
    }

    private fun regenerateArchiveCommands(
        lastIndex: Int,
        currentStateIndex: Int,
        totalBlocks: Int,
    ) {

        if (lastIndex == -1) {
            Log.d(
                TAG,
                "🆕 Первый запуск архива: формируем полный цикл от $currentStateIndex → 0 " +
                        "и $totalBlocks → $currentStateIndex"
            )

            // От currentStateIndex до 0
            for (i in currentStateIndex downTo 0) {
                archiveInitCommands.trySend(commandFactory.readBlock(i))
            }
            // От totalBlocks - 1 до currentStateIndex + 1
            for (i in totalBlocks - 1 downTo currentStateIndex + 1) {
                archiveInitCommands.trySend(commandFactory.readBlock(i))
            }

            return
        }

        var index = currentStateIndex

        while (true) {
            archiveCommands.trySend(commandFactory.readBlock(index))
            if (index == lastIndex) break
            index = (index - 1 + totalBlocks) % totalBlocks
        }

    }

    private fun <E> clearChannel(channel: Channel<E>) {
        while (channel.tryReceive().isSuccess) Unit
    }

    companion object {
        private const val ARCHIVE_INIT_BLOCK_COUNT = 192
        private const val ARCHIVE_COMMAND_COUNT = 384
        private val TAG = CommandRepository::class.java.simpleName
    }
}
