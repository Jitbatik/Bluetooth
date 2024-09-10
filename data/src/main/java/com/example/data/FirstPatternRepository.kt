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
    //    fun getStateSocket(): Flow<Boolean> {
//        return bluetoothSocketProvider.bluetoothSocket
//            .map { socket -> socket != null }
//            .distinctUntilChanged()
//    }

    private fun getValidSocket(): BluetoothSocket? {
        val socket = bluetoothSocketProvider.bluetoothSocket.value
        if (socket == null || !socket.isConnected) {
            val error = "Socket is ${if (socket == null) "null" else "not connected"}"
            Log.d("ttt", error)
            return null
        }
        return socket
    }

    private val _testHub = MutableStateFlow<List<TestDataList>>(emptyList())

    fun getData(): Flow<List<CharData>> {
        return _testHub.map { testDataList ->
            testDataList.mapToListCharData()
        }
    }

    private fun List<TestDataList>.mapToListCharData(): List<CharData> {
        return this.flatMap { it.listByte }.map {
            CharData(charByte = it, colorByte = 1.toByte(), backgroundByte = 0.toByte())
        }
    }

    private fun ByteArray.mapToTestDataList(): TestDataList {
        val index = this[5].toInt()
        val listByte = this.drop(6)
        return TestDataList(index = index, listByte = listByte)
    }

    private fun addTestData(data: TestDataList) {
        _testHub.update { currentList ->
            val mutableList = currentList.toMutableList()
            val existingIndex = mutableList.indexOfFirst { it.index == data.index }
            if (existingIndex != -1) {
                mutableList[existingIndex] = data
            } else {
                mutableList.add(data)
            }
            mutableList
        }
    }

//    private fun printTestHub() {
//        _testHub.forEach { testData ->
//            Log.d(
//                "TEST_HUB",
//                "Index: ${testData.index}, Data: ${testData.listByte.joinToString(", ")}"
//            )
//        }
//    }


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
                    Log.d(
                        FIRST_PATTERN_REPOSITORY,
                        "Received from stream in flow: ${data.joinToString(" ")}"
                    )
                    addTestData(data.mapToTestDataList())

                }
        }

        while (true) {
            val missingIndex = checkTest()

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

            delay(100)
        }
        canRead.value = false
        //printTestHub()
    }

    private fun checkTest(): Int {
        val requiredIndices = (0..19).toList()
        val presentIndices = _testHub.value.map { it.index }
        for (index in requiredIndices) {
            if (index !in presentIndices) {
                return index
            }
        }
        return -1
    }


}
