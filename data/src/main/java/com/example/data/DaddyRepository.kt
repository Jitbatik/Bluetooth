package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import java.io.IOException
import javax.inject.Inject

private const val DADDY = "ROMEO"

class DaddyRepository @Inject constructor() {
    fun sendToStream(socket: BluetoothSocket, value: ByteArray) {
        try {
            socket.outputStream?.let { outputStream ->
                outputStream.write(value)
                outputStream.flush()
                Log.d(DADDY, "Written to stream: ${value.joinToString(" ")}")
            }
        } catch (e: IOException) {
            Log.e(DADDY, "Error sending data to stream", e)
        }
    }

    fun readFromStream(socket: BluetoothSocket) = flow {
        while (socket.isConnected && currentCoroutineContext().isActive) {
            try {
                val buffer = ByteArray(1024)
                socket.inputStream?.let { inputStream ->
                    val bytesRead = inputStream.read(buffer)
                    Log.d(DADDY, "Read from stream: ${buffer.copyOf(bytesRead).joinToString(" ")}")
                    emit(buffer.copyOf(bytesRead))
                }
            } catch (e: IOException) {
                Log.e(DADDY, "Error reading data to stream", e)
            }
        }
    }.onCompletion {
        if (!currentCoroutineContext().isActive) {
            Log.d(DADDY, "Flow cancelled, closing socket")
        }
    }
        .catch { err -> Log.e(DADDY, "ERROR", err) }
        .flowOn(Dispatchers.IO)
}