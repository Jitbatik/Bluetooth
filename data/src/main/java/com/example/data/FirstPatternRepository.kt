package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.data.bluetooth.utils.mapToListCharDataFromArray
import com.example.domain.model.CharData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
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
            delay(1000)
            sendToStream(byteArray)
            val request = readFromStream()

            if (request != null && request.isNotEmpty()) {
                listByteArray.add(request)
                currentIndex = (currentIndex + 1) //% 20
            }

        }
        emit(listByteArray.mapToListCharDataFromArray())
    }

    private fun getSocket(): BluetoothSocket? {
        return bluetoothSocketProvider.bluetoothSocket.value
    }

    private fun readFromStream(): ByteArray? {
        val buffer = ByteArray(10)
        val socket = getSocket()
        if (socket == null) {
            Log.d(FIRST_PATTERN_REPOSITORY, "Socket is null")
            return null
        }
        if (!socket.isConnected) {
            Log.d(FIRST_PATTERN_REPOSITORY, "Socket is not connected")
            return null
        }
        val inputStream = socket.inputStream
        if (inputStream == null) {
            Log.d(FIRST_PATTERN_REPOSITORY, "InputStream is null")
            return null
        }


        try {
            inputStream.read(buffer)
            Log.d(FIRST_PATTERN_REPOSITORY, "Read from stream: [${buffer.joinToString(" ")}]")
            return buffer
        } catch (e: IOException) {
            Log.e(FIRST_PATTERN_REPOSITORY, "Error reading stream", e)
            return null
        }
    }

    private suspend fun sendToStream(value: ByteArray): Result<Boolean> {
        val socket = getSocket()
        return withContext(Dispatchers.IO) {
            try {
                if (socket == null) {
                    Log.d(FIRST_PATTERN_REPOSITORY, "Socket is null")
                    return@withContext Result.failure(SecurityException("No connected socket"))
                }
                if (!socket.isConnected) {
                    return@withContext Result.failure(SecurityException("Socket is not connected"))
                }

                val outputStream = socket.outputStream
                    ?: return@withContext Result.failure(IOException("Output stream is null"))

                outputStream.write(value)
                outputStream.flush()

                Log.d(
                    FIRST_PATTERN_REPOSITORY,
                    "WRITTEN TO STREAM: ${value.joinToString(" ")}"
                )
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