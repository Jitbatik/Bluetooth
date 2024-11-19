package com.example.bluetooth.presentation.view.home

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            is HomeEvent.ButtonClick -> generateCommand(event.pressedButton)
            is HomeEvent.Press -> createCoordinateCommand(event)
        }

        sendData(value = command)
    }

    private fun sendData(value: ByteArray) {
        Log.d(tag, "Send data: ${value.joinToString(" ") { (it.toInt() and 0xFF).toString() }}")
        viewModelScope.launch {
            try {
                exchangeDataRepository.sendToStream(value = value)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send data: ${e.message}")
            }
        }
    }


    private val baseUART =
        byteArrayOf(0xFE.toByte(), 0x08.toByte(), 0x80.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00)
    private val baseModbus =
        byteArrayOf(0x01.toByte(), 0x17.toByte(), 0x04.toByte())

    private fun generateCommand(event: ButtonType): ByteArray {
        return when (event) {
            is ButtonType.F -> generateFCommand(event.number)
            ButtonType.Menu -> baseUART + byteArrayOf(0, 4)
            ButtonType.Mode -> baseUART + byteArrayOf(0, 20)
            ButtonType.Enter -> baseUART + byteArrayOf(80, 0)
            ButtonType.Cancel -> baseUART + byteArrayOf(10, 0)
            ButtonType.Archive -> baseUART + byteArrayOf(2, 0)
            ButtonType.FButton -> baseUART + byteArrayOf(0, 40)
            is ButtonType.Arrow -> generateArrowCommand(event.direction)
            ButtonType.SecondaryCancel -> baseModbus + byteArrayOf(
                0x12.toByte(), 0x1D.toByte(), 0x00.toByte(), 0x10.toByte()
            )

            ButtonType.SecondaryEnter -> baseModbus + byteArrayOf(
                0x16.toByte(), 0x1D.toByte(), 0x00.toByte(), 0x80.toByte()
            )

            ButtonType.SecondaryUp -> baseModbus + byteArrayOf(
                0x19.toByte(), 0x1D.toByte(), 0x01.toByte(), 0x00.toByte()
            )

            ButtonType.SecondaryDown -> baseModbus + byteArrayOf(
                0x1D.toByte(), 0x1D.toByte(), 0x08.toByte(), 0x00.toByte()
            )

            ButtonType.Burner -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x20.toByte(), 0x00.toByte()
            )

            ButtonType.Close -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x08.toByte()
            )

            ButtonType.Open -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x01.toByte()
            )

            ButtonType.SecondaryF -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x40.toByte(), 0x00.toByte()
            )

            ButtonType.Stop -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x04.toByte(), 0x00.toByte()
            )

            //TODO: у кнопки F двойной код + какая-то кнопка
            //TODO: вторая клавиатура доделать + рефакторинг
            ButtonType.One -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x04.toByte()
            )

            ButtonType.Two -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x80.toByte(), 0x00.toByte()
            )

            ButtonType.Three -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x10.toByte(), 0x00.toByte()
            )

            ButtonType.Four -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x02.toByte(), 0x00.toByte()
            )

            ButtonType.Five -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x20.toByte()
            )

            ButtonType.Six -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x40.toByte()
            )

            ButtonType.Seven -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x08.toByte()
            )

            ButtonType.Eight -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x01.toByte()
            )

            ButtonType.Nine -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x04.toByte(), 0x00.toByte()
            )

            ButtonType.Zero -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x00.toByte(), 0x02.toByte()
            )

            ButtonType.Minus -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x08.toByte(), 0x00.toByte()
            )

            ButtonType.Point -> baseModbus + byteArrayOf(
                0x00, 0x00, 0x20.toByte(), 0x00.toByte()
            )
        }
    }

    private fun generateFCommand(number: Int): ByteArray {
        return when (number) {
            1 -> baseUART + byteArrayOf(4, 0)
            2 -> baseUART + byteArrayOf(0, 80)
            3 -> baseUART + byteArrayOf(0, 10)
            4 -> baseUART + byteArrayOf(0, 2)
            5 -> baseUART + byteArrayOf(20, 0)
            6 -> baseUART + byteArrayOf(40, 0)
            7 -> baseUART + byteArrayOf(8, 0)
            8 -> baseUART + byteArrayOf(1, 0)
            else -> byteArrayOf()
        }
    }

    private fun generateArrowCommand(direction: ButtonType.ArrowDirection): ByteArray {
        return when (direction) {
            ButtonType.ArrowDirection.Up -> baseModbus + byteArrayOf(0, 1) // ▲
            ButtonType.ArrowDirection.Down -> baseModbus + byteArrayOf(0, 8) // ▼
        }
    }

    private fun createCoordinateCommand(event: HomeEvent.Press): ByteArray {
        Log.d(tag, "Coordinate pressed: column ${event.column}, row ${event.row} ")
        return baseModbus + byteArrayOf(event.column.toByte(), event.row.toByte(), 0x00, 0x00)
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