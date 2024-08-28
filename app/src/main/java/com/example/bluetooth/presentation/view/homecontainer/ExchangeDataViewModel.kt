package com.example.bluetooth.presentation.view.homecontainer

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.utils.mapToListCharUIModel
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
    private val _data = MutableStateFlow<List<CharUI>>(emptyList())
    val data: StateFlow<List<CharUI>> = _data

    private val _sentence =
        "Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен"


    init {
        returnTemplateData()
        //observeSocketState()
    }

    fun requestPacketData() {
        viewModelScope.launch {
            exchangeDataRepository.requestData().collect { data ->
                Log.d(EXCHANGE_VIEWMODEL, "REQUEST DATA")
                _data.value = data.mapToListCharUIModel()
            }
        }
    }

//    private fun continuouslyRequestData() {
//        viewModelScope.launch {
//            while (true) {
//                try {
//                    requestPacketData()
//                } catch (e: Exception) {
//                    Log.e(EXCHANGE_VIEWMODEL, "Error requesting data", e)
//                }
//                delay(5000)
//            }
//        }
//    }

//    private fun observeSocketState() {
//        Log.d(EXCHANGE_VIEWMODEL, "Subscribe to a stream connected")
//        viewModelScope.launch {
//            exchangeDataRepository.getStateSocket()
//                .distinctUntilChanged()
//                .collectLatest { socketState ->
//                    Log.d(EXCHANGE_VIEWMODEL, "Socket state changed: $socketState")
//                    if (socketState) {
//                        Log.d(EXCHANGE_VIEWMODEL, "Socket connected, requesting data")
//                        continuouslyRequestData()
//                    }
//                }
//        }
//    }

    private fun returnTemplateData() {
        fun getRandomColor(): Color {
            val r = (0..255).random()
            val g = (0..255).random()
            val b = (0..255).random()
            return Color(r, g, b)
        }

        val newData = _sentence.map { char ->
            CharUI(
                char = char,
                color = getRandomColor(),
                background = getRandomColor()
            )
        }
        _data.value = newData
    }
}


