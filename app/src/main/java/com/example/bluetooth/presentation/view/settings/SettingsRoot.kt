package com.example.bluetooth.presentation.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.R
import com.example.bluetooth.model.ChartSettingsUI
import com.example.bluetooth.model.DescriptionSettings
import com.example.bluetooth.model.SignalColor
import com.example.bluetooth.model.SignalSettingsUI
import com.example.bluetooth.presentation.view.settings.components.LineChartSettings
import com.example.bluetooth.presentation.view.settings.components.WirelessNetworkSettings
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.WirelessNetworkState
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun SettingsRoot(
    viewModel: SettingsViewModel = viewModel()
) {
    val chartSettings by viewModel.chartSettingsUI.collectAsState()
    val wirelessNetworkState by viewModel.wirelessNetworkState.collectAsState()
    val onEvents: (SettingsEvent) -> Unit = remember {
        { event -> viewModel.onEvents(event) }
    }
    Settings(
        chartSettingsUI = chartSettings,
        state = wirelessNetworkState,
        onEvents = onEvents
    )
}

@Composable
private fun Settings(
    chartSettingsUI: ChartSettingsUI,
    state: WirelessNetworkState,
    onEvents: (SettingsEvent) -> Unit
) {
    val bluetoothDescriptionSettings = DescriptionSettings(
        title = stringResource(R.string.title_bluetooth),
        descriptionSwitch = stringResource(R.string.description_switch_bluetooth),
        hintTextField = stringResource(R.string.hint_text_bluetooth)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            WirelessNetworkSettings(
                state = state,
                onEvent = onEvents,
                descriptionSettings = bluetoothDescriptionSettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            LineChartSettings(
                chartSettingsUI = chartSettingsUI,
                onEvents = onEvents,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsPreview() = BluetoothTheme {
    Surface {
        Settings(
            chartSettingsUI = ChartSettingsUI(
                title = "Тестовые параметры",
                description = "Тестовое описание",
                signals = listOf(
                    SignalSettingsUI("test", "Тест", true, Color.Green)
                )
            ),
            state = WirelessNetworkState(),
            onEvents = {}
        )
    }
}