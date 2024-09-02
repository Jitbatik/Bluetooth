package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.data.bluetooth.utils.mapToListCharDataFromArray
import com.example.domain.model.CharData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject

private const val FIRST_PATTERN_REPOSITORY = "MANAGE_DATA_REPOSITORY_IMPL"

class FirstPatternRepository @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
) {

//    fun getStateSocket(): Flow<Boolean> {
//        return bluetoothSocketProvider.bluetoothSocket
//            .map { socket -> socket != null }
//            .distinctUntilChanged()
//    }


    suspend fun requestData(): Flow<List<CharData>> = flow {
        var currentIndex = 0
        val listByteArray = mutableListOf<ByteArray>()

        while (currentIndex < 20) {
            val command = byteArrayOf(
                0xFE.toByte(),
                0x08.toByte(),
                0x00,
                0x00,
                0x00,
                currentIndex.toByte(),
                0x00,
                0x00,
                0x00,
                0x00
            )

            var request: ByteArray? = null

            while (request == null) {
                if (request == null) {
                    Log.d(FIRST_PATTERN_REPOSITORY, "tttttt")
                }
                sendToStream(command)
                request = withTimeoutOrNull(4000) {
                    withContext(Dispatchers.IO) {
                        readFromStream()
                    }
                }
                if (request != null) {
                    listByteArray.add(request)
                }
            }
            currentIndex++
        }

        emit(listByteArray.mapToListCharDataFromArray())
    }


    private fun getSocket(): BluetoothSocket? {
        return bluetoothSocketProvider.bluetoothSocket.value
    }


    private fun readFromStream(): ByteArray? {
        val socket = getSocket()

        if (socket == null || !socket.isConnected) {
            Log.d(
                FIRST_PATTERN_REPOSITORY,
                "Socket is ${if (socket == null) "null" else "not connected"}"
            )
            return null
        }

        return try {
            val inputStream = socket.inputStream ?: return null
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            if (bytesRead != -1) {
                buffer.copyOf(bytesRead).also {
                    Log.d(FIRST_PATTERN_REPOSITORY, "Read from stream: [${it.joinToString(" ")}]")
                }
            } else {
                null
            }


        } catch (e: IOException) {
            Log.e(FIRST_PATTERN_REPOSITORY, "Error reading stream", e)
            null
        }
    }


//    private fun readFromStream(): ByteArray? {
//        val socket = getSocket()
//
//        if (socket == null || !socket.isConnected) {
//            Log.d(
//                FIRST_PATTERN_REPOSITORY,
//                "Socket is ${if (socket == null) "null" else "not connected"}"
//            )
//            return null
//        }
//
//        val executor = Executors.newSingleThreadExecutor()
//        val future: Future<ByteArray?> = executor.submit(Callable {
//            try {
//                val inputStream = socket.inputStream ?: return@Callable null
//                val buffer = ByteArray(1024)
//                val bytesRead = inputStream.read(buffer)
//                return@Callable if (bytesRead != -1) {
//                    buffer.copyOf(bytesRead).also {
//                        Log.d(
//                            FIRST_PATTERN_REPOSITORY,
//                            "Read from stream: [${it.joinToString(" ")}]"
//                        )
//                    }
//                } else {
//                    null
//                }
//            } catch (e: IOException) {
//                Log.e(FIRST_PATTERN_REPOSITORY, "Error reading stream", e)
//                null
//            }
//        })
//
//        return try {
//            // Тайм-аут 4 секунды
//            future.get(4, TimeUnit.SECONDS)
//
//        } catch (e: TimeoutException) {
//            Log.e(FIRST_PATTERN_REPOSITORY, "Read timed out after 4 seconds")
//            null
//        } catch (e: Exception) {
//            Log.e(FIRST_PATTERN_REPOSITORY, "Error reading stream", e)
//            null
//        } finally {
//            future.cancel(true)
//            executor.shutdown() // Прекращаем работу executor
//        }
//    }


    private suspend fun sendToStream(value: ByteArray): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val socket = getSocket()

            if (socket == null || !socket.isConnected) {
                val error = "Socket is ${if (socket == null) "null" else "not connected"}"
                Log.d(FIRST_PATTERN_REPOSITORY, error)
                return@withContext Result.failure(SecurityException(error))
            }

            return@withContext try {
                val outputStream = socket.outputStream ?: return@withContext Result.failure(
                    IOException("Output stream is null")
                )
                outputStream.write(value)
                outputStream.flush()
                Log.d(FIRST_PATTERN_REPOSITORY, "Written to stream: ${value.joinToString(" ")}")
                Result.success(true)
            } catch (e: IOException) {
                Log.e(FIRST_PATTERN_REPOSITORY, "Error sending data to stream", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(FIRST_PATTERN_REPOSITORY, "Unexpected error", e)
                Result.failure(e)
            }
        }
    }
}