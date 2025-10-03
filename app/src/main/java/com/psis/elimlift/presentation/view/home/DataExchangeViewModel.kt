package com.psis.elimlift.presentation.view.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psis.elimlift.EventHandler
import com.psis.transfer.protocol.data.LiftRepository
import com.psis.transfer.protocol.domain.usecase.SendCommandUseCase
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
    liftRepository: LiftRepository,
    private val sendCommandUseCase: SendCommandUseCase,
    private val eventHandler: EventHandler<HomeEvent, ByteArray>,
) : ViewModel() {
    private val _data: StateFlow<List<DataUI>> =
        liftRepository.observeLiftData()
            .map { byteDataList -> filterByteDataList(byteDataList) }
            .mapToHomeDataUI()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    val data: StateFlow<List<DataUI>> = _data

    private fun filterByteDataList(byteDataList: List<Byte>) =
        if (byteDataList.size < 208) byteDataList
        else byteDataList.subList(128, 208)


    private fun Flow<List<Byte>>.mapToHomeDataUI(): Flow<List<DataUI>> = map { charDataList ->
        charDataList.map { charData ->
            DataUI(
                data = String(byteArrayOf(charData), Charsets.ISO_8859_1),
                color = Color.Black,
                background = Color(0xFF0000AA),
            )
        }
    }

    fun onEvents(event: HomeEvent) {
        sendData(eventHandler.handle(event))
    }

    private fun sendData(command: ByteArray) {
        viewModelScope.launch { sendCommandUseCase(command) }
    }
}