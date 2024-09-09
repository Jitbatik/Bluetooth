package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FIRST_PATTERN_REPOSITORY = "MANAGE_DATA_REPOSITORY_IMPL"

/**
 * Проверяем состояние сокета, если socket != null,
 * то создаем корутину и слушаем данные
 * в ином случае закрываем корутину
 * */


class FirstPatternRepository @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
    private val daddyRepository: DaddyRepository
) {
//    fun getStateSocket(): Flow<Boolean> {
//        return bluetoothSocketProvider.bluetoothSocket
//            .map { socket -> socket != null }
//            .distinctUntilChanged()
//    }

    private fun getSocket(): BluetoothSocket? {
        return bluetoothSocketProvider.bluetoothSocket.value
    }

    private fun getValidSocket(): BluetoothSocket? {
        val socket = getSocket()
        if (socket == null || !socket.isConnected) {
            val error = "Socket is ${if (socket == null) "null" else "not connected"}"
            Log.d("ttt", error)
            return null
        }
        return socket
    }

    suspend fun requestData() {
        val socket = getValidSocket() ?: return

        val listeningJob = CoroutineScope(Dispatchers.IO).launch {
            daddyRepository.readFromStream(socket = socket).collect { data ->
                Log.d(
                    FIRST_PATTERN_REPOSITORY,
                    "Received from stream in flow: ${data.joinToString(" ")}"
                )
            }
        }
        var currentIndex = 0
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
            daddyRepository.sendToStream(socket = socket, value = command)

            currentIndex++
            delay(100)
        }

        listeningJob.cancelAndJoin()
    }
}


//    suspend fun sendToStream(value: ByteArray): Result<Boolean> {
//        return withContext(Dispatchers.IO) {
//            val socket = getSocket()
//            if (socket == null || !socket.isConnected) {
//                val error = "Socket is ${if (socket == null) "null" else "not connected"}"
//                Log.d(FIRST_PATTERN_REPOSITORY, error)
//                return@withContext Result.failure(SecurityException(error))
//            }
//
//            return@withContext try {
//                socket.outputStream?.let { outputStream ->
//                    outputStream.write(value)
//                    Log.d(FIRST_PATTERN_REPOSITORY, "Written to stream: ${value.joinToString(" ")}")
//                    Result.success(true)
//                } ?: Result.failure(IOException("Output stream is null"))
//            } catch (e: IOException) {
//                Log.e(FIRST_PATTERN_REPOSITORY, "Error sending data to stream", e)
//                Result.failure(e)
//            } catch (e: Exception) {
//                Log.e(FIRST_PATTERN_REPOSITORY, "Unexpected error", e)
//                Result.failure(e)
//            }
//        }
//    }
