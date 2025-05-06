package com.example.bluetooth.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.bluetooth.data.utils.BluetoothSocketProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

class DataStreamRepository @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSocketStream(): Flow<ByteArray> =
        bluetoothSocketProvider.bluetoothSocket
            .flatMapLatest { socket -> if (socket == null) emptyFlow() else readFromStream(socket) }


    fun sendToStream(value: ByteArray) {
        val socket = bluetoothSocketProvider.bluetoothSocket.value
        if (socket == null) {
            Log.e(TAG, "BluetoothSocket is null. Cannot send data.")
            return
        }

        try {
            socket.outputStream?.let { outputStream ->
                outputStream.write(value)
                outputStream.flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error sending data to stream", e)
        }
    }

    private fun readFromStream(socket: BluetoothSocket): Flow<ByteArray> = flow {
        try {
            val buffer = ByteArray(1024)
            val inputStream = socket.inputStream

            while (true) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) {
                    Log.d(TAG, "End of stream reached. Closing flow.")
                    break
                }
                val data = buffer.copyOf(bytesRead)
                emit(data)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading data from stream", e)
        }
    }.onCompletion {
        Log.d(TAG, "readFromStream flow completed.")
    }.catch { e ->
        Log.e(TAG, "Error in readFromStream flow", e)
    }.flowOn(Dispatchers.IO)

    companion object {
        private val TAG = DataStreamRepository::class.java.simpleName
    }
}