package com.example.bluetooth.presentation.view.home

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.model.CharData
import com.example.transfer.model.ControllerConfig
import com.example.transfer.model.KeyMode
import com.example.transfer.model.Range
import com.example.transfer.model.Rotate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataExchangeViewModel @Inject constructor(
    private val protocolDataRepository: ProtocolDataRepository,
    private val eventHandler: EventHandler
) : ViewModel() {
    private val tag = DataExchangeViewModel::class.java.simpleName

    //TODO: Убрать после тестов
    private val _test = protocolDataRepository.getAnswerTest()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        )
    val test: StateFlow<String> = _test

    private val _data = protocolDataRepository.observeData()
        .mapToCharUI()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initializeCharUIList()
        )
    val data: StateFlow<List<CharUI>> = _data

    private val _controllerConfig = protocolDataRepository.observeControllerConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ControllerConfig(
                range = Range(startRow = 0, endRow = 0, startCol = 0, endCol = 0),
                keyMode = KeyMode.NONE,
                rotate = Rotate.PORTRAIT,
                isBorder = false,
            )
        )
    val controllerConfig: StateFlow<ControllerConfig> = _controllerConfig

    private fun Flow<List<CharData>>.mapToCharUI(): Flow<List<CharUI>> = map { charDataList ->
        charDataList.map { charData ->
            CharUI(
                char = String(byteArrayOf(charData.charByte), Charsets.ISO_8859_1)[0],
                color = pal16[charData.colorByte],
                background = pal16[charData.backgroundByte],
            )
        }
    }

    private fun initializeCharUIList(): List<CharUI> {
        Log.d(tag, "Start observe data from Bluetooth")
//        val initialData =
//            "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"

        val byteArray = byteArrayOf(
            0x80.toByte(), 0x82.toByte(), 0x84.toByte(), 0x85.toByte(), 0x86.toByte(),
            0x87.toByte(), 0x27.toByte(), 0x32.toByte(),
            0xCE.toByte(), 0x95.toByte(), 0xDE.toByte(), 0xA0.toByte(), 0xE0.toByte(),
        )
        val resultArray = ByteArray(280)
        for (i in resultArray.indices) {
            resultArray[i] = byteArray[i % byteArray.size]
        }

        return resultArray.map { byte ->
            CharUI(
                char = String(byteArrayOf(byte), Charsets.ISO_8859_1)[0],
                color = Color.Black,
                background = getRandomColor(),
            )
        }
    }

    private fun getRandomColor(): Color {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color(r, g, b)
    }

    fun onEvents(event: HomeEvent) {
        Log.d(tag, "An event has arrived: ${event::class.simpleName}")
        val command = eventHandler.handleEvent(event)
        sendData(command)
    }

    private fun sendData(command: ByteArray) {
        viewModelScope.launch {
            try {
                protocolDataRepository.sendToStream(value = command)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send data: ${e.message}")
            }
        }
    }

    companion object {
        private val pal16 = arrayOf(
            Color(0xFF000000), Color(0xFF0000AA), Color(0xFF00AA00), Color(0xFF00AAAA),
            Color(0xFFAA0000), Color(0xFFAA00AA), Color(0xFFAA5500), Color(0xFFAAAAAA),
            Color(0xFF555555), Color(0xFF5555FF), Color(0xFF55FF55), Color(0xFF55FFFF),
            Color(0xFFFF5555), Color(0xFFFF55FF), Color(0xFFFFFF55), Color(0xFFFFFFFF)
        )
    }
}