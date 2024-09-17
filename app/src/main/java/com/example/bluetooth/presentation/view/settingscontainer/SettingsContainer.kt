package com.example.bluetooth.presentation.view.settingscontainer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.model.DescriptionSettings
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun SettingsContainer(
    //viewModel: ExchangeDataViewModel = viewModel(),
) {
    SettingsContainerContent()
}

@Composable
fun SettingsContainerContent() {
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
            WirelessNetworkSettings(descriptionSettings = bluetoothDescriptionSettings)
            Spacer(modifier = Modifier.height(8.dp))
//            if (BuildConfig.DEBUG) {
//                LogHistory()
//            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsContainerPreview() = BluetoothTheme {
    Surface {
        SettingsContainerContent()
    }
}