package com.example.bluetooth.presentation.view.homecontainer

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.ExchangeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EXCHANGE_VIEWMODEL = "EXCHANGE_VIEWMODEL"

@HiltViewModel
class ExchangeDataViewModel @Inject constructor(
    private val exchangeDataRepository: ExchangeDataRepository,
) : ViewModel() {
    private val _data = MutableStateFlow<List<CharUIModel>>(emptyList())
    val data: StateFlow<List<CharUIModel>> = _data

    private val _sentence =
        "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"


    init {
        returnTemplateData()
    }

    private fun returnTemplateData() {
        fun getRandomColor(): Color {
            val r = (0..255).random()
            val g = (0..255).random()
            val b = (0..255).random()
            return Color(r, g, b)
        }

        val newData = _sentence.map { char ->
            CharUIModel(
                char = char,
                charColor = getRandomColor(),
                charBackground = getRandomColor()
            )
        }
        _data.value = newData
    }

    fun requestPacketData() {
        viewModelScope.launch {
            exchangeDataRepository.requestData().collect { data ->
                Log.d(EXCHANGE_VIEWMODEL, "REQUEST DATA")
                addColor(data)
            }
        }

    }

    private fun addColor(data: List<Byte>) {
        val newData = data.map { char ->
            CharUIModel(
                char = char.toInt().toChar(),
                charColor = Color.Black,
                charBackground = Color.Transparent
            )
        }
        Log.d(EXCHANGE_VIEWMODEL, "Convert DATA $newData")
        _data.value = newData
    }

//    private fun sendData(value: ByteArray) {
//        viewModelScope.launch {
//            val result = exchangeDataRepository.sendToStream(value)
//            if (result.isSuccess) {
//                Log.d(EXCHANGE_VIEWMODEL, "Data sent successfully: ${value.joinToString(" ")}")
//            } else {
//                Log.e(
//                    EXCHANGE_VIEWMODEL,
//                    "Failed to send data: $value, Error: ${result.exceptionOrNull()}"
//                )
//            }
//        }
//    }
}


