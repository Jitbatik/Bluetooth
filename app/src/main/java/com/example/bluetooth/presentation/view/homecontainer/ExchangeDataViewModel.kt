package com.example.bluetooth.presentation.view.homecontainer

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
class ExchangeDataViewModel @Inject constructor(
    private val exchangeDataRepository: ExchangeDataRepository,
) : ViewModel() {
    private val _data = exchangeDataRepository.observeData()
        .mapToCharUI()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = returnTemplateData()
        )
    val data: StateFlow<List<CharUI>> = _data

    private fun Flow<List<CharData>>.mapToCharUI(): Flow<List<CharUI>> = map { charDataList ->
        charDataList.map { charData ->
            CharUI(
                char = charData.charByte.toInt().toChar()
            )
        }
    }


    private fun returnTemplateData(): List<CharUI> {
        Log.d(TAG, "Start observe data from Bluetooth")
        val sentence =
            "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

        fun getRandomColor(): Color {
            val r = (0..255).random()
            val g = (0..255).random()
            val b = (0..255).random()
            return Color(r, g, b)
        }

        val newData = sentence.map { char ->
            CharUI(
                char = char,
                color = Color.Black,//getRandomColor(),
                background = getRandomColor()
            )
        }
        return newData
    }

    private fun sendData(value: ByteArray) {
        Log.d(TAG, "Send data: ${value.joinToString(" ")}")
        viewModelScope.launch {
            exchangeDataRepository.sendToStream(value = value)
        }
    }

    fun onEvents(event: UIEvents) {
        Log.d(TAG, "An event has arrived")
        val command = generateCommand(event)
        sendData(command)
    }

    private fun generateCommand(event: UIEvents): ByteArray {
        val baseCommand =
            byteArrayOf(0xFE.toByte(), 0x08.toByte(), 0x80.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00)

        val commandMap = mapOf(
            ClickButtonF1 to byteArrayOf(0x04.toByte(), 0x00),
            ClickButtonF2 to byteArrayOf(0x00, 0x80.toByte()),
            ClickButtonF3 to byteArrayOf(0x04.toByte(), 0x00),
            ClickButtonF4 to byteArrayOf(0x00, 0x02.toByte()),
            ClickButtonF5 to byteArrayOf(0x20.toByte(), 0x00),
            ClickButtonF6 to byteArrayOf(0x40.toByte(), 0x00),
            ClickButtonF7 to byteArrayOf(0x08.toByte(), 0x00),
            ClickButtonF8 to byteArrayOf(0x01.toByte(), 0x00),
            ClickButtonMenu to byteArrayOf(0x00, 0x04.toByte()),
            ClickButtonMode to byteArrayOf(0x00, 0x20.toByte()),
            ClickButtonInput to byteArrayOf(0x80.toByte(), 0x00),
            ClickButtonCancel to byteArrayOf(0x10.toByte(), 0x00),
            ClickButtonArchive to byteArrayOf(0x02.toByte(), 0x00),
            ClickButtonF to byteArrayOf(0x00, 0x40.toByte()),
            ClickButtonUpArrow to byteArrayOf(0x00, 0x01.toByte()),
            ClickButtonDownArrow to byteArrayOf(0x00, 0x08.toByte())
        )

        val commandSuffix = commandMap[event] ?: byteArrayOf()

        Log.d(TAG, "Generate Command: ${commandSuffix.joinToString(" ")}")

        return baseCommand + commandSuffix
    }

    companion object {
        private val TAG = ExchangeDataViewModel::class.java.simpleName
    }
}