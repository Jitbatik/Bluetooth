package com.example.bluetooth.presentation.view.homecontainer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.ExchangeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EXCHANGE_VIEWMODEL = "CONNECT_VIEWMODEL"

@HiltViewModel
class ExchangeDataViewModel @Inject constructor(
    private val exchangeDataRepository: ExchangeDataRepository,
) : ViewModel() {


    private val _data = MutableStateFlow<List<Pair<Char, Pair<Color, Color>>>>(emptyList())
    val data: StateFlow<List<Pair<Char, Pair<Color, Color>>>> = _data


    init {
        observeSocketState()
    }

    private fun observeSocketState() {
        Log.d(EXCHANGE_VIEWMODEL, "Subscribe to a stream")
        viewModelScope.launch {
            exchangeDataRepository.data.collect { newData ->
                _data.value = newData
            }
        }
    }
}
