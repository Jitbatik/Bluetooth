package com.example.bluetooth.presentation.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluetooth.R
import com.example.bluetooth.model.ChartSettingsUI
import com.example.bluetooth.model.DescriptionSettings
import com.example.bluetooth.model.SignalSettingsUI
import com.example.bluetooth.presentation.view.settings.components.LineChartSettings
import com.example.bluetooth.presentation.view.settings.components.WirelessNetworkSettings
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.SettingsState
import com.example.bluetooth.presentation.view.settings.model.WirelessBluetoothMask
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun SettingsRoot(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val onEvents: (SettingsEvent) -> Unit = remember {
        { event -> viewModel.onEvents(event) }
    }
    Settings(
        state = state,
        onEvents = onEvents,
    )
}

@Composable
private fun Settings(
    state: SettingsState,
    onEvents: (SettingsEvent) -> Unit
) {
    val bluetoothDescriptionSettings = DescriptionSettings(
        title = stringResource(R.string.title_bluetooth),
        descriptionSwitch = stringResource(R.string.description_switch_bluetooth),
        hintTextField = stringResource(R.string.hint_text_bluetooth)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                WirelessNetworkSettings(
                    state = state.wirelessBluetoothMask,
                    onEvent = onEvents,
                    descriptionSettings = bluetoothDescriptionSettings
                )
            }

            item {
                LineChartSettings(
                    chartSettingsUI = state.chartSettings,
                    onEvents = onEvents,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsPreview() = BluetoothTheme {
    val state = SettingsState(
        chartSettings = ChartSettingsUI(
            title = "Тестовые параметры",
            description = "Тестовое описание",
            signals = listOf(
                SignalSettingsUI("test", "Тест", true, Color.Green)
            )
        ),
        wirelessBluetoothMask = WirelessBluetoothMask(
            isEnabled = false,
            mask = ""
        ),
    )
    Surface {
        Settings(
            state = state,
            onEvents = { }
        )
    }
}