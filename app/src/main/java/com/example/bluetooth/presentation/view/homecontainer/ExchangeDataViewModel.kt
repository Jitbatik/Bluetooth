package com.example.bluetooth.presentation.view.homecontainer

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.ExchangeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

private const val EXCHANGE_VIEWMODEL = "EXCHANGE_VIEWMODEL"

@HiltViewModel
class ExchangeDataViewModel @Inject constructor(
    private val exchangeDataRepository: ExchangeDataRepository,
) : ViewModel() {
    private val _isConnected = MutableStateFlow(false)

    private val _data = MutableStateFlow<List<Pair<Char, Pair<Color, Color>>>>(emptyList())
    val data: StateFlow<List<Pair<Char, Pair<Color, Color>>>> = _data

    private var dataStreamJob: Job? = null

    private val _sentence =
        MutableStateFlow("Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен")


    init {
        returnSentenceData()
        observeSocketState()
    }


    private fun returnSentenceData() {
        fun getRandomColor(): Color {
            val r = (0..255).random()
            val g = (0..255).random()
            val b = (0..255).random()
            return Color(r, g, b)
        }

        val newData = _sentence.value.map { char ->
            Pair(char, Pair(getRandomColor(), getRandomColor()))
        }
        _data.value = newData
    }


    private fun observeSocketState() {
        Log.d(EXCHANGE_VIEWMODEL, "Subscribe to a stream connected")
        viewModelScope.launch {
            exchangeDataRepository.getStateSocket()
                .distinctUntilChanged()
                .collectLatest { socketState ->
                    _isConnected.value = socketState
                    if (socketState) {
                        dataStreamJob?.cancel()
                        dataStreamJob = launch { observeDataFromSocket() }
                    } else {
                        returnSentenceData()
                        Log.d(EXCHANGE_VIEWMODEL, "Unsubscribe to data stream from socket")
                        dataStreamJob?.cancel()
                    }
                }
        }
    }

    private suspend fun observeDataFromSocket() {
        Log.d(EXCHANGE_VIEWMODEL, "Subscribe to data stream from socket")
        try {
            exchangeDataRepository.readFromStream(canRead = true)
                .collect { byteArray ->
                    Log.d(EXCHANGE_VIEWMODEL, "BYTEARRAY: ${byteArray.joinToString("")}")
                    val parseData = parseData(byteArray)
                    _data.value = parseData
                }

        } catch (e: IOException) {
            Log.e(EXCHANGE_VIEWMODEL, "Error reading data from socket", e)
            returnSentenceData()
        } finally {
            returnSentenceData()
        }

    }

    private suspend fun requestPacketData() {
        var currentIndex = 0

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

            sendData(command.toString())

            currentIndex = (currentIndex + 1) % 21

            delay(1000L)
        }
//        val command = listOf(
//                0xFE,
//                0x08,
//                0x00,
//                0x00,
//                0x00,
//                0x00,
//                0x00,
//                0x00,
//                0x00,
//                0x00
//            )
//        sendData(command.toString())
    }

    private fun parseData(byteArray: ByteArray): List<Pair<Char, Pair<Color, Color>>> {
        Log.d(EXCHANGE_VIEWMODEL, "Data from socket: $byteArray")
        return emptyList()
    }

    private fun sendData(value: String) {
        viewModelScope.launch {
            val result = exchangeDataRepository.sendToStream(value)
            if (result.isSuccess) {
                Log.d(EXCHANGE_VIEWMODEL, "Data sent successfully: $value")
            } else {
                Log.e(EXCHANGE_VIEWMODEL, "Failed to send data: $value, Error: ${result.exceptionOrNull()}")
            }
        }
    }
}


