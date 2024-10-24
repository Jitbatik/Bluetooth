package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
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

    private fun getSocket(): BluetoothSocket? {
        return bluetoothSocketProvider.bluetoothSocket.value
    }

    private fun getInputStream(): InputStream? {
        return bluetoothSocketProvider.bluetoothSocket.value?.inputStream
    }

    private fun getOutputStream(): OutputStream? {
        return bluetoothSocketProvider.bluetoothSocket.value?.outputStream
    }

    override suspend fun requestData(): Flow<List<Byte>> = flow {
        var currentIndex = 0
        val listByteArray = mutableListOf<ByteArray>()

        while (currentIndex < 20) {
            val command = listOf(
                0xFE,
                0x08,
                0x00,
                0x00,
                0x00,
                currentIndex,
                0x00,
                0x00,
                0x00,
                0x00
            )
            val byteArray = command.map { it.toByte() }.toByteArray()
            delay(100)
            sendToStream(byteArray)
            val test = readFromStreamVer2()

            if (test != null && test.isNotEmpty()) {
                listByteArray.add(test)
                currentIndex = (currentIndex + 1) //% 20
            }

        }
        emit(parsData(listByteArray))
    }

    private fun parsData(listByteArray: List<ByteArray>): List<Byte> {
        return listByteArray.flatMap { byteArray ->
            byteArray.drop(6)
        }
    }


    private fun readFromStreamVer2(): ByteArray? {
        val buffer = ByteArray(10)
        val socket = getSocket()
        if (socket == null) {
            Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "Socket is null")
            return null
        }
        if (!socket.isConnected) {
            Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "Socket is not connected")
            return null
        }
        val inputStream = socket.inputStream
        if (inputStream == null) {
            Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "InputStream is null")
            return null
        }


        try {
            inputStream.read(buffer)
            Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "Read from stream: [${buffer.joinToString(" ")}]")
            return buffer
        } catch (e: IOException) {
            Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error reading stream", e)
            return null
        }
    }

    override fun readFromStream(canRead: Boolean): Flow<ByteArray> {
        return flow {
            val inputStream = getInputStream()
            if (inputStream == null) {
                Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, " InputStream is null")
                return@flow
            }

            while (canRead && currentCoroutineContext().isActive) {
                if (bluetoothSocketProvider.bluetoothSocket.value?.isConnected == true) {
                    try {
                        val buffer = ByteArray(10)
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
                    Log.d(
                        EXCHANGE_DATA_REPOSITORY_IMPL,
                        "Socket is not connected or InputStream is null"
                    )
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
            Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "CLOSE STREAMS")
        } catch (e: IOException) {
            Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error closing streams", e)
        }
    }


    override suspend fun sendToStream(value: ByteArray): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val socket = bluetoothSocketProvider.bluetoothSocket.value
                    ?: return@withContext Result.failure(SecurityException("No connected socket"))

                if (!socket.isConnected) {
                    return@withContext Result.failure(SecurityException("Socket is not connected"))
                }

                val outputStream = getOutputStream()
                    ?: return@withContext Result.failure(IOException("Output stream is null"))

                outputStream.write(value)
                outputStream.flush()

                Log.d(
                    EXCHANGE_DATA_REPOSITORY_IMPL,
                    "WRITTEN TO STREAM: ${value.joinToString(" ")}"
                )
                Result.success(true)
            } catch (e: IOException) {
                Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error sending data to stream", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Unexpected error", e)
                Result.failure(e)
            }
        }
    }
}