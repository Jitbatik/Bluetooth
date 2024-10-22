package com.example.bluetooth.presentation.view.home

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CharData
import com.example.domain.repository.ExchangeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DeviceExchangeViewModel @Inject constructor(
    private val exchangeDataRepository: ExchangeDataRepository,
) : ViewModel() {
    private val tag = DeviceExchangeViewModel::class.java.simpleName

    private val _data = exchangeDataRepository.observeData()
        .mapToCharUI()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initializeCharUIList()
        )
    val data: StateFlow<List<CharUI>> = _data

    private fun Flow<List<CharData>>.mapToCharUI(): Flow<List<CharUI>> = map { charDataList ->
        charDataList.map { charData ->
            CharUI(
                char = String(byteArrayOf(charData.charByte), Charsets.ISO_8859_1)[0]
            )
        }
    }

    private fun initializeCharUIList(): List<CharUI> {
        Log.d(tag, "Start observe data from Bluetooth")
//        val initialData =
//            "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

        val byteArray = byteArrayOf(
            0x80.toByte(),
            0x82.toByte(),
            0x84.toByte(),
            0x85.toByte(),
            0x86.toByte(),
            0x87.toByte(),
            0xCE.toByte(),
            0xCE.toByte(),
            0xCE.toByte(),
            0xCE.toByte(),
            0x27.toByte(),
            0xCE.toByte(),
            0x32.toByte(),
            0xCE.toByte(),
            0xCE.toByte(),
            0xCE.toByte(),
            0x95.toByte(),
            0xDE.toByte(),
            0xA0.toByte(),
            0xE0.toByte()
        )

        fun getRandomColor(): Color {
            val r = (0..255).random()
            val g = (0..255).random()
            val b = (0..255).random()
            return Color(r, g, b)
        }

        val charUIList = byteArray.map { byte ->
            CharUI(
                char = String(byteArrayOf(byte), Charsets.ISO_8859_1)[0],
                color = Color.Black,
                background = getRandomColor()
            )
        }
        return charUIList
    }

    private fun sendData(value: ByteArray) {
        Log.d(tag, "Send data: ${value.joinToString(" ") { (it.toInt() and 0xFF).toString() }}")
        viewModelScope.launch {
            exchangeDataRepository.sendToStream(value = value)
        }
    }

    fun onEvents(event: HomeEvent) {
        Log.d(tag, "An event has arrived")
        when (event) {
            is HomeEvent.ButtonClick -> {
                Log.d(tag, "This is event: ${event.pressedButton}")
                val command = generateCommand(event.pressedButton)
                sendData(command)
            }

            is HomeEvent.TextPositionTapped -> {
                Log.d(tag, "\"Coordinate pressed: column ${event.column}, row ${event.row}\"")
            }
        }
    }

    private fun generateCommand(event: ButtonType): ByteArray {
        val baseCommand =
            byteArrayOf(0xFE.toByte(), 0x08.toByte(), 0x80.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00)

        val commandSuffix = when (event) {
            is ButtonType.F -> {
                when (event.number) {
                    1 -> byteArrayOf(4, 0)
                    2 -> byteArrayOf(0, 80)
                    3 -> byteArrayOf(0, 10)
                    4 -> byteArrayOf(0, 2)
                    5 -> byteArrayOf(20, 0)
                    6 -> byteArrayOf(40, 0)
                    7 -> byteArrayOf(8, 0)
                    8 -> byteArrayOf(1, 0)
                    else -> byteArrayOf()
                }
            }

            is ButtonType.Menu -> byteArrayOf(0, 4)
            is ButtonType.Mode -> byteArrayOf(0, 20)
            is ButtonType.Enter -> byteArrayOf(80, 0)
            is ButtonType.Cancel -> byteArrayOf(10, 0)
            is ButtonType.Archive -> byteArrayOf(2, 0)
            is ButtonType.FButton -> byteArrayOf(0, 40)
            is ButtonType.Arrow -> {
                when (event.direction) {
                    is ButtonType.ArrowDirection.Up -> byteArrayOf(0, 1) // ▲
                    is ButtonType.ArrowDirection.Down -> byteArrayOf(0, 8) // ▼
                }
            }
        }

        Log.d(tag, "Generate Command: ${commandSuffix.joinToString(" ")}")

        return baseCommand + commandSuffix
    }

}