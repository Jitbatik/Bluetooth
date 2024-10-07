package com.example.bluetooth.presentation.view.home

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.utils.UIEvents
import com.example.bluetooth.utils.UIEvents.ClickButtonArchive
import com.example.bluetooth.utils.UIEvents.ClickButtonCancel
import com.example.bluetooth.utils.UIEvents.ClickButtonDownArrow
import com.example.bluetooth.utils.UIEvents.ClickButtonF
import com.example.bluetooth.utils.UIEvents.ClickButtonF1
import com.example.bluetooth.utils.UIEvents.ClickButtonF2
import com.example.bluetooth.utils.UIEvents.ClickButtonF3
import com.example.bluetooth.utils.UIEvents.ClickButtonF4
import com.example.bluetooth.utils.UIEvents.ClickButtonF5
import com.example.bluetooth.utils.UIEvents.ClickButtonF6
import com.example.bluetooth.utils.UIEvents.ClickButtonF7
import com.example.bluetooth.utils.UIEvents.ClickButtonF8
import com.example.bluetooth.utils.UIEvents.ClickButtonInput
import com.example.bluetooth.utils.UIEvents.ClickButtonMenu
import com.example.bluetooth.utils.UIEvents.ClickButtonMode
import com.example.bluetooth.utils.UIEvents.ClickButtonUpArrow

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
                char = charData.charByte.toInt().toChar()
            )
        }
    }

    private fun initializeCharUIList(): List<CharUI> {
        Log.d(tag, "Start observe data from Bluetooth")
        val initialData =
            "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

        fun getRandomColor(): Color {
            val r = (0..255).random()
            val g = (0..255).random()
            val b = (0..255).random()
            return Color(r, g, b)
        }

        val charUIList = initialData.map { char ->
            CharUI(
                char = char,
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

    fun onEvents(event: UIEvents) {
        Log.d(tag, "An event has arrived")
        val command = generateCommand(event)
        sendData(command)
    }

    private fun generateCommand(event: UIEvents): ByteArray {
        val baseCommand =
            byteArrayOf(0xFE.toByte(), 0x08.toByte(), 0x80.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00)

        val commandMap = mapOf(
            ClickButtonF1 to byteArrayOf(4, 0),
            ClickButtonF2 to byteArrayOf(0, 80),
            ClickButtonF3 to byteArrayOf(0, 10),
            ClickButtonF4 to byteArrayOf(0, 2),
            ClickButtonF5 to byteArrayOf(20, 0),
            ClickButtonF6 to byteArrayOf(40, 0),
            ClickButtonF7 to byteArrayOf(8, 0),
            ClickButtonF8 to byteArrayOf(1, 0),
            ClickButtonMenu to byteArrayOf(0, 4),
            ClickButtonMode to byteArrayOf(0, 20),
            ClickButtonInput to byteArrayOf(80, 0),
            ClickButtonCancel to byteArrayOf(10, 0),
            ClickButtonArchive to byteArrayOf(2, 0),
            ClickButtonF to byteArrayOf(0, 40),
            ClickButtonUpArrow to byteArrayOf(0, 1),
            ClickButtonDownArrow to byteArrayOf(0, 8)
        )


        val commandSuffix = commandMap[event] ?: byteArrayOf()

        Log.d(tag, "Generate Command: ${commandSuffix.joinToString(" ")}")

        return baseCommand + commandSuffix
    }
}