package com.example.bluetooth.presentation.view.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.domain.SettingsManager
import com.example.bluetooth.presentation.view.settings.model.BluetoothEvent
import com.example.bluetooth.presentation.view.settings.model.ChartXmlPickerEvent
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.SettingsState
import com.example.bluetooth.presentation.view.settings.model.SignalEvent
import com.example.bluetooth.presentation.view.settings.model.WirelessBluetoothMask
import com.example.bluetooth.presentation.view.settings.utils.chartSettingsMapToUI
import com.example.transfer.chart.data.ChartSettingsRepository
import com.example.transfer.filePick.domain.usecase.ObserveSingleCommonXmlFileUseCase
import com.example.transfer.filePick.domain.usecase.ObserveVersionXmlFileUseCase
import com.example.transfer.filePick.domain.usecase.ObserveSelectedFileVersionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsManager: SettingsManager,
    chartSettingsRepository: ChartSettingsRepository,
    observeVersionFiles: ObserveVersionXmlFileUseCase,
    observeSingleCommonFile: ObserveSingleCommonXmlFileUseCase,
    private val signalHandler: SignalEventHandler,
    private val bluetoothHandler: BluetoothEventHandler,
    private val xmlPickerHandler: ChartXmlEventHandler,
    observeSelectedFileVersionUseCase: ObserveSelectedFileVersionUseCase,
) : ViewModel() {

    val state = createStateFlow(
        chartSettingsRepository,
        settingsManager,
        observeVersionFiles,
        observeSingleCommonFile,
        observeSelectedFileVersionUseCase
    )

    private fun createStateFlow(
        chartSettingsRepository: ChartSettingsRepository,
        settingsManager: SettingsManager,
        observeVersionFiles: ObserveVersionXmlFileUseCase,
        observeSingleCommonFile: ObserveSingleCommonXmlFileUseCase,
        test: ObserveSelectedFileVersionUseCase,
    ): StateFlow<SettingsState> {
        val chartSettingsFlow = chartSettingsRepository.chartSettings
            .map { it.chartSettingsMapToUI() }

        val bluetoothFlow = combine(
            settingsManager.isEnabledChecked(),
            settingsManager.getBluetoothMask()
        ) { isEnabled, mask -> WirelessBluetoothMask(isEnabled, mask) }

        val filesFlow = combine(
            observeSingleCommonFile(),
            observeVersionFiles(),
        ) { common, version -> Pair(common, version) }

        return combine(
            chartSettingsFlow,
            bluetoothFlow,
            filesFlow,
            test()
        ) { chartSettings, bluetooth, (commonFiles, versionFiles), selectedFile ->
            SettingsState(
                chartSettings = chartSettings,
                wirelessBluetoothMask = bluetooth,
                commonFiles = commonFiles,
                versionFiles = versionFiles,
                selectedFileName = selectedFile
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SettingsStateDefaults.getDefault()
        )
    }

    fun onEvents(event: SettingsEvent) = viewModelScope.launch {
        when (event) {
            is SignalEvent -> signalHandler.handle(event)
            is BluetoothEvent -> bluetoothHandler.handle(event)
            is ChartXmlPickerEvent -> xmlPickerHandler.handle(event)
        }
    }
}