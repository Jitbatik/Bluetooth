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
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _data = MutableStateFlow<List<Pair<Char, Pair<Color, Color>>>>(emptyList())
    val data: StateFlow<List<Pair<Char, Pair<Color, Color>>>> = _data

    private val _sentence =
        MutableStateFlow("Процессор: СР6786   v105  R2  17.10.2023СКБ ПСИС www.psis.ruПроцессор остановлен")

    private fun getRandomColor(): Color {
        val r = (0..255).random()
        val g = (0..255).random()
        val b = (0..255).random()
        return Color(r, g, b)
    }

    private fun updateData() {
        val newData = _sentence.value.map { char ->
            Pair(char, Pair(getRandomColor(), getRandomColor()))
        }
        _data.value = newData
    }

    init {
        observeSocketState()
        updateData()

    }

    private fun observeSocketState() {
        Log.d(EXCHANGE_VIEWMODEL, "Subscribe to a stream connected")
        viewModelScope.launch {
            exchangeDataRepository.isSocket
                .collect { isConnected ->
                    _isConnected.value = isConnected
                }
        }
    }
}
