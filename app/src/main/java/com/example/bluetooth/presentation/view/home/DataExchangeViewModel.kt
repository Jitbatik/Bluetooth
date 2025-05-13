package com.example.bluetooth.presentation.view.home

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.transfer.domain.ProtocolDataRepository
import com.example.transfer.domain.parameters.Type
import com.example.transfer.domain.parameters.usecase.ObserveParametersUseCase
import com.example.transfer.model.ByteData
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
import navigation.NavigationItem
import javax.inject.Inject


@HiltViewModel
class DataExchangeViewModel @Inject constructor(
    navigationStateHolder: NavigationStateHolder,
    private val protocolDataRepository: ProtocolDataRepository,
    observeControllerDataUseCase: ObserveParametersUseCase,
    private val eventHandler: EventHandler,
) : ViewModel() {
    private val tag = DataExchangeViewModel::class.java.simpleName

    private val _test = protocolDataRepository.getAnswerTest()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        )
    val test: StateFlow<String> = _test


    private val typeFlow: StateFlow<Type> = navigationStateHolder.currentScreen
        .map { screen ->
            when (screen) {
                NavigationItem.Home -> Type.READ
                NavigationItem.ParametersDashboard -> Type.READ
                else -> Type.NOTHING
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Type.NOTHING
        )

    private val _data: StateFlow<List<DataUI>> =
        observeControllerDataUseCase.execute(typeFlow)
            .map { byteDataList -> filterByteDataList(byteDataList) }
            .mapToHomeDataUI()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = HomeDataDefaults.getDefault()
            )
    val data: StateFlow<List<DataUI>> = _data

    private fun filterByteDataList(byteDataList: List<ByteData>) =
        byteDataList.subList(128, 208)

    private fun Flow<List<ByteData>>.mapToHomeDataUI(): Flow<List<DataUI>> = map { charDataList ->
        charDataList.map { charData ->
            DataUI(
                data = String(byteArrayOf(charData.byte), Charsets.ISO_8859_1),
                color = pal16[charData.colorByte],
                background = pal16[charData.backgroundByte],
            )
        }
    }


    val isConnected: StateFlow<Boolean> = observeControllerDataUseCase.execute(typeFlow)
        .map { charData ->
            charData.isNotEmpty()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

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

    fun onEvents(event: HomeEvent) {
        sendData(eventHandler.handleEvent(event))
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