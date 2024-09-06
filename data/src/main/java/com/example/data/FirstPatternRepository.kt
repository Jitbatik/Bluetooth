package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private const val FIRST_PATTERN_REPOSITORY = "MANAGE_DATA_REPOSITORY_IMPL"
/**
 * Проверяем состояние сокета, если socket != null,
 * то создаем корутину и слушаем данные
 * в ином случае закрываем корутину
 * */
class FirstPatternRepository @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
) {

    private val totalPackets = 20
    private val packetsMap = mutableMapOf<Int, DataPacket>().apply {
        for (i in 0 until totalPackets) {
            put(i, DataPacket(DataPacketState.Unknown))
        }
    }


    fun getStateSocket(): Flow<Boolean> {
        return bluetoothSocketProvider.bluetoothSocket
            .map { socket -> socket != null }
            .distinctUntilChanged()
            .onEach { isConnected ->
                if (isConnected) {
                    listening()
                }
            }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var listeningJob: Job? = null

    private fun listening() {
        val socket = getSocket()
        if (socket == null || !socket.isConnected) {
            val error = "Socket is ${if (socket == null) "null" else "not connected"}"
            Log.d(FIRST_PATTERN_REPOSITORY, error)
            return
        }

        if (listeningJob != null && listeningJob?.isActive == true) {
            return
        }

        listeningJob = coroutineScope.launch {
            val inputStream = socket.inputStream ?: return@launch
            val buffer = ByteArray(1024)
            try {
                while (isActive && socket.isConnected) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead != -1) saveTableData(buffer.copyOf(bytesRead))
                }
            } catch (e: IOException) {
                if (!socket.isConnected) {
                    Log.d(FIRST_PATTERN_REPOSITORY, "Socket was closed, stopping reading")
                } else {
                    Log.e(FIRST_PATTERN_REPOSITORY, "Error reading stream", e)
                }
            } finally {
                stopListening()
            }
        }
    }

    private fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        Log.d(FIRST_PATTERN_REPOSITORY, "Stopped listening")
    }

    private fun saveTableData(value: ByteArray) {
        val index = value[5].toInt()
        Log.d(
            FIRST_PATTERN_REPOSITORY,
            "Received packet index=$index,\n\t data=[${value.joinToString(" ")}]"
        )

        val existingPacket = packetsMap[index]

        if (existingPacket == null || !existingPacket.data.contentEquals(value))
            packetsMap[index] = DataPacket(DataPacketState.Received, value)
    }

    private fun getSocket(): BluetoothSocket? {
        return bluetoothSocketProvider.bluetoothSocket.value
    }

    suspend fun requestData() {
        getValidSocket() ?: return
        sendCommands()
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

    private suspend fun sendCommands() {
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
            sendToStream(command)
            currentIndex++
            delay(100)
        }
    }

    suspend fun sendToStream(value: ByteArray): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val socket = getSocket()
            if (socket == null || !socket.isConnected) {
                val error = "Socket is ${if (socket == null) "null" else "not connected"}"
                Log.d(FIRST_PATTERN_REPOSITORY, error)
                return@withContext Result.failure(SecurityException(error))
            }

            return@withContext try {
                socket.outputStream?.let { outputStream ->
                    outputStream.write(value)
                    Log.d(FIRST_PATTERN_REPOSITORY, "Written to stream: ${value.joinToString(" ")}")
                    Result.success(true)
                } ?: Result.failure(IOException("Output stream is null"))
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


//private fun handleCompleteData() {
//    packetsMap.forEach { (index, packet) ->
//        when (packet.state) {
//            DataPacketState.Received -> {
//                Log.d(
//                    FIRST_PATTERN_REPOSITORY,
//                    "Packet index: $index, data: ${packet.data.joinToString(" ")}"
//                )
//            }
//
//            DataPacketState.Unknown -> {
//                Log.d(FIRST_PATTERN_REPOSITORY, "Packet index: $index is missing (Unknown)")
//            }
//        }
//    }
//}

//suspend fun requestData(): Flow<List<CharData>> = flow {
//        val socket = getSocket()
//        if (socket == null || !socket.isConnected) {
//            val error = "Socket is ${if (socket == null) "null" else "not connected"}"
//            Log.d("ttt", error)
//            return@flow
//        }
//        launchReadingData()
//
//        var currentIndex = 0
//        val listByteArray = mutableListOf<ByteArray>()
//
//        while (currentIndex < 20) {
//            val command = byteArrayOf(
//                0xFE.toByte(),
//                0x08.toByte(),
//                0x00,
//                0x00,
//                0x00,
//                currentIndex.toByte(),
//                0x00,
//                0x00,
//                0x00,
//                0x00
//            )
//            sendToStream(command)
////            val request = withContext(Dispatchers.IO) {
////                dataChannel.receive()
////            }
////
////            listByteArray.add(request)
//
//            //var request: ByteArray? = null
//
////            while (request == null) {
////                sendToStream(command)
////                request = withContext(Dispatchers.IO) {
////                        readFromStream()
////                    }
////                if (request != null) {
////                    listByteArray.add(request)
////                }
////            }
//            currentIndex++
//        }
//        emit(listByteArray.mapToListCharDataFromArray())
//    }

//private fun readFromStream(): ByteArray? {
//    val socket = getSocket()
//
//    if (socket == null || !socket.isConnected) {
//        Log.d(
//            FIRST_PATTERN_REPOSITORY,
//            "Socket is ${if (socket == null) "null" else "not connected"}"
//        )
//        return null
//    }
//
//    return try {
//        val inputStream = socket.inputStream ?: return null
//        val buffer = ByteArray(1024)
//        val bytesRead = inputStream.read(buffer)
//        if (bytesRead != -1) {
//            buffer.copyOf(bytesRead).also {
//                Log.d(FIRST_PATTERN_REPOSITORY, "Read from stream: [${it.joinToString(" ")}]")
//            }
//        } else {
//            null
//        }
//
//
//    } catch (e: IOException) {
//        Log.e(FIRST_PATTERN_REPOSITORY, "Error reading stream", e)
//        null
//    }
//}