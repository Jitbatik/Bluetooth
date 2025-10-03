package com.psis.transfer.protocol.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class DataStreamHelpers @Inject constructor() {
    fun sendToStream(socket: BluetoothSocket?, value: ByteArray) {
        runCatching {
            socket?.outputStream?.apply {
                write(value)
                flush()
            } ?: Log.e(TAG, "BluetoothSocket is null. Cannot send data.")
        }.onFailure {
            Log.e(TAG, "Error sending data to stream", it)
        }
    }

    fun readFromStream(socket: BluetoothSocket): Flow<ByteArray> = flow {
        val buffer = ByteArray(1024)
        val inputStream = socket.inputStream
        while (true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break
            emit(buffer.copyOf(bytesRead))
        }
    }.onCompletion { Log.d(TAG, "readFromStream flow completed.") }
        .catch { e -> Log.e(TAG, "Error in readFromStream flow", e) }
        .flowOn(Dispatchers.IO)

    companion object {
        private val TAG = DataStreamHelpers::class.java.simpleName
    }
}