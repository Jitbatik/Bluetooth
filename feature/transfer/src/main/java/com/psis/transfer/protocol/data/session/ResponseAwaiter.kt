package com.psis.transfer.protocol.data.session

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicReference

/**
 * Отвечает за ожидание одного ответа от устройства.
 * Гарантирует, что одновременно активен только один ожидатель.
 */
class ResponseAwaiter {
    private val current = AtomicReference<CompletableDeferred<Unit>?>()

    suspend fun await(timeoutMs: Long): Boolean {
        val deferred = CompletableDeferred<Unit>().also { current.set(it) }
        val result = withTimeoutOrNull(timeoutMs) { deferred.await() }
        current.set(null)
        return result != null
    }

    fun complete() = current.getAndSet(null)?.complete(Unit)
    fun cancel() = current.getAndSet(null)?.cancel()
}