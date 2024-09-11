package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.domain.model.CharData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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
    private fun getValidSocket(): BluetoothSocket? {
        val socket = bluetoothSocketProvider.bluetoothSocket.value
        if (socket == null || !socket.isConnected) {
            val error = "Socket is ${if (socket == null) "null" else "not connected"}"
            Log.d("ttt", error)
            return null
        }
        return socket
    }

    private val _dataPacketsFlow = MutableStateFlow<List<DataPacket>>(emptyList())
    fun getData(): Flow<List<CharData>> {
        return _dataPacketsFlow
            .filter { it.size == MAX_PACKET_SIZE }
            .map { it.mapToListCharData() }
    }

    companion object {
        private const val MAX_PACKET_SIZE = 20
        private const val RETRY_DELAY_MS = 100L
    }

    private fun List<DataPacket>.mapToListCharData(): List<CharData> {
        return this.flatMap { it.dataBytes }.map {
            CharData(charByte = it, colorByte = 1.toByte(), backgroundByte = 0.toByte())
        }
    }
    //    fun getStateSocket(): Flow<Boolean> {
//        return bluetoothSocketProvider.bluetoothSocket
//            .map { socket -> socket != null }
//            .distinctUntilChanged()
//    }

    private fun ByteArray.mapToDataPacket(): DataPacket {
        val index = this[5].toInt()
        val listByte = this.drop(6)
        return DataPacket(index = index, dataBytes = listByte)
    }

    private fun addDataPacket(data: DataPacket) {
        _dataPacketsFlow.update { currentList ->
            val mutableList = currentList.toMutableList()
            val existingIndex = mutableList.indexOfFirst { it.index == data.index }

            if (existingIndex != -1) {
                val existingData = mutableList[existingIndex]
                if (existingData != data) {
                    mutableList[existingIndex] = data
                }
            } else {
                mutableList.add(data)
            }
            mutableList
        }
    }

    suspend fun requestData() {
        val socket = getValidSocket() ?: return
        val canRead = MutableStateFlow(true)

        CoroutineScope(Dispatchers.IO).launch {
            daddyRepository.readFromStream(socket = socket, canRead = canRead)
                .cancellable()
                .collect { data ->
                    if (!canRead.value) {
                        cancel()
                    }
                    addDataPacket(data.mapToDataPacket())
                }
        }

        while (true) {
            val missingIndex = checkMissingPackage()

            if (missingIndex == -1) {
                break
            }

            val command = byteArrayOf(
                0xFE.toByte(),
                0x08.toByte(),
                0x00,
                0x00,
                0x00,
                missingIndex.toByte(),
                0x00,
                0x00,
                0x00,
                0x00
            )

            daddyRepository.sendToStream(socket = socket, value = command)

            delay(RETRY_DELAY_MS)
        }
        //canRead.value = false
        //printDataPackets()
    }


    /**
     * Function to check for missing package
     *  Searches for an index in the range 0 to 19 that is not in the list.
     *  @return if(missing package) package index else -1
     * */
    private fun checkMissingPackage(): Int {
        val requiredIndices = (0 until MAX_PACKET_SIZE).toList()
        val presentIndices = _dataPacketsFlow.value.map { it.index }
        for (index in requiredIndices) {
            if (index !in presentIndices) {
                return index
            }
        }
        return -1
    }


//    private fun printDataPackets() {
//        _dataPacketsFlow.value.forEach { testData ->
//            Log.d(
//                "TEST_HUB",
//                "Index: ${testData.index}, Data: ${testData.dataBytes.joinToString(", ")}"
//            )
//        }
//        Log.d(FIRST_PATTERN_REPOSITORY, "САЙз ${_dataPacketsFlow.value.size}")
//    }
}



