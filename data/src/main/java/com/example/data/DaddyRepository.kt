package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import java.io.IOException
import javax.inject.Inject

private const val DADDY = "ROMEO"

class DaddyRepository @Inject constructor(
) {
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

    fun readFromStream(socket: BluetoothSocket, canRead: StateFlow<Boolean>) = flow {
        while (canRead.value) {
            try {
                val buffer = ByteArray(1024)
                socket.inputStream?.let { inputStream ->
                    val bytesRead = inputStream.read(buffer)
                    Log.d(DADDY, "Read from stream: ${buffer.copyOf(bytesRead).joinToString(" ")}")
                    if (bytesRead != -1) emit(buffer.copyOf(bytesRead))

                }
            } catch (e: IOException) {
                Log.e(DADDY, "Error reading data to stream", e)
                break
            }
        }

    }.onCompletion {
        if (!canRead.value) {
            Log.d(DADDY, "Flow completed as canRead is false")
        } else {
            Log.d(DADDY, "Flow completed successfully")
        }
    }
        .catch { err -> Log.e(DADDY, "ERROR", err) }
        .flowOn(Dispatchers.IO)
}