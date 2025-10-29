package com.psis.transfer.protocol.data.session

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable

/**
 * Контейнер, инкапсулирующий состояние активной Bluetooth-сессии
 */
class SessionScope(
    val socket: BluetoothSocket,
    parentJob: Job
) : Closeable {
    val scope = CoroutineScope(SupervisorJob(parentJob) + Dispatchers.IO)

    override fun close() {
        runCatching {
            scope.cancel("Session closed")
            socket.close()
        }.onFailure { Log.w(TAG, "Ошибка при закрытии сокета", it) }
    }

    companion object {
        private const val TAG = "SessionScope"
    }
}