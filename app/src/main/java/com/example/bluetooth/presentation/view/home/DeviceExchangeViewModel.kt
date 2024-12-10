package com.example.bluetooth.presentation.view.home

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.view.home.state.ButtonType
import com.example.domain.model.CharData
import com.example.domain.model.ControllerConfig
import com.example.domain.model.KeyMode
import com.example.domain.model.Range
import com.example.domain.model.Rotate
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

    //TODO: Убрать после тестов
    private val _test = exchangeDataRepository.getAnswerTest()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        )
    val test: StateFlow<String> = _test

    private val _data = exchangeDataRepository.observeData()
        .mapToCharUI()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initializeCharUIList()
        )
    val data: StateFlow<List<CharUI>> = _data

    private val _controllerConfig = exchangeDataRepository.observeControllerConfig()
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
        val command = when (event) {
            is HomeEvent.ButtonClick -> processButtonCommand(activeButtons = event.buttons)
            is HomeEvent.Press -> generateCommand(colum = event.column, row = event.row)
        }
        sendData(command = command)
    }

    private fun processButtonCommand(
        activeButtons: List<ButtonType>,
    ): ByteArray {
        return if (activeButtons.isNotEmpty()) {
            val combinedCommand = activeButtons
                .map { button -> handleButton(button) }
                .reduce { acc, array -> acc.orWith(array) }
            baseModbus + combinedCommand.toByteArray()
        } else baseModbus + intArrayOf(0x00, 0x00, 0x00, 0x00).toByteArray()
    }

    private fun IntArray.orWith(other: IntArray): IntArray {
        return this.zip(other) { a, b -> a or b }.toIntArray()
    }

    private fun IntArray.toByteArray(): ByteArray = map { it.toByte() }.toByteArray()

    private fun sendData(command: ByteArray) {
        Log.d(tag, "Send data: $command")
        viewModelScope.launch {
            try {
                exchangeDataRepository.sendToStream(value = command)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send data: ${e.message}")
            }
        }
    }

    private val baseModbus = byteArrayOf(0x01.toByte(), 0x17.toByte(), 0x04.toByte())

    private fun generateCommand(colum: Int, row: Int): ByteArray =
        baseModbus + byteArrayOf(colum.toByte(), row.toByte(), 0x00, 0x00)

    private fun handleButton(type: ButtonType): IntArray {
        val command = when (type) {
            ButtonType.BURNER -> intArrayOf(0x00, 0x00, 0x20, 0x00)
            ButtonType.F -> intArrayOf(0x00, 0x00, 0x40, 0x00)
            ButtonType.CANCEL -> intArrayOf(0x12, 0x1D, 0x00, 0x10)
            ButtonType.ENTER -> intArrayOf(0x16, 0x1D, 0x00, 0x80)
            ButtonType.ARROW_UP -> intArrayOf(0x19, 0x1D, 0x01, 0x00)
            ButtonType.ARROW_DOWN -> intArrayOf(0x1D, 0x1D, 0x08, 0x00)
            ButtonType.ONE -> intArrayOf(0x00, 0x00, 0x00, 0x04)
            ButtonType.TWO -> intArrayOf(0x00, 0x00, 0x80, 0x00)
            ButtonType.THREE -> intArrayOf(0x00, 0x00, 0x10, 0x00)
            ButtonType.FOUR -> intArrayOf(0x00, 0x00, 0x02, 0x00)
            ButtonType.FIVE -> intArrayOf(0x00, 0x00, 0x00, 0x20)
            ButtonType.SIX -> intArrayOf(0x00, 0x00, 0x00, 0x40)
            ButtonType.SEVEN, ButtonType.CLOSE -> intArrayOf(0x00, 0x00, 0x00, 0x08)
            ButtonType.EIGHT, ButtonType.OPEN -> intArrayOf(0x00, 0x00, 0x00, 0x01)
            ButtonType.NINE, ButtonType.STOP -> intArrayOf(0x00, 0x00, 0x04, 0x00)
            ButtonType.ZERO -> intArrayOf(0x00, 0x00, 0x00, 0x02)
            ButtonType.MINUS -> intArrayOf(0x00, 0x00, 0x08, 0x00)
            ButtonType.POINT -> intArrayOf(0x00, 0x00, 0x20, 0x00)
        }
        return command
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