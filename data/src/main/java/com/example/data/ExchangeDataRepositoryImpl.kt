package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

private const val EXCHANGE_DATA_REPOSITORY_IMPL = "MANAGE_DATA_REPOSITORY_IMPL"

class ExchangeDataRepositoryImpl @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
) : ExchangeDataRepository {

    override fun getStateSocket(): Flow<Boolean> {
        return bluetoothSocketProvider.bluetoothSocket
            .map { socket -> socket != null }
            .distinctUntilChanged()
    }

    private fun getInputStream(): InputStream? {
        return bluetoothSocketProvider.bluetoothSocket.value?.inputStream
    }

    private fun getOutputStream(): OutputStream? {
        return bluetoothSocketProvider.bluetoothSocket.value?.outputStream
    }

    override fun readFromStream(canRead: Boolean): Flow<ByteArray> {
        return flow {
            val buffer = ByteArray(4 * 1_024)
            while (canRead) {
                val inputStream = getInputStream()
                if (inputStream != null && bluetoothSocketProvider.bluetoothSocket.value?.isConnected == true) {
                    try {
                        val bytesRead = withContext(Dispatchers.IO) {
                            inputStream.read(buffer)
                        }
                        if (bytesRead > 0) {
                            emit(buffer.copyOf(bytesRead))
                        }
                    } catch (e: IOException) {
                        Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error reading stream", e)
                        break
                    }
                } else {
                    // Сокет не подключен или inputStream null
                    break
                }
            }
        }.onCompletion {
            closeStreams()
        }
    }

    private fun closeStreams() {
        try {
            getInputStream()?.close()
            getOutputStream()?.close()
        } catch (e: IOException) {
            Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error closing streams", e)
        }
    }


//    override suspend fun sendToStream(value: String): Result<Boolean> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val socket = socket ?: return@withContext Result.failure(SecurityException("No connected socket"))
//                if (!socket.isConnected) {
//                    return@withContext Result.failure(SecurityException("Socket is not connected"))
//                }
//
//                val outputStream = _outputStream ?: return@withContext Result.failure(IOException("Output stream is null"))
//
//                outputStream.write(value.toByteArray())
//                outputStream.flush()
//
//                Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "WRITTEN TO STREAM")
//                Result.success(true)
//            } catch (e: IOException) {
//                Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error sending data to stream", e)
//                Result.failure(e)
//            }
//        }
//    }

}