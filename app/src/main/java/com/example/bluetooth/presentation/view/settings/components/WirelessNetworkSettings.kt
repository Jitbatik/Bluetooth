package com.example.bluetooth.presentation.view.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.model.DescriptionSettings
import com.example.bluetooth.presentation.view.settings.model.BluetoothEvent
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.bluetooth.presentation.view.settings.model.WirelessBluetoothMask
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun WirelessNetworkSettings(
    state: WirelessBluetoothMask,
    onEvent: (SettingsEvent) -> Unit,
    descriptionSettings: DescriptionSettings
) {
    ExpandableItem(
        title = descriptionSettings.title,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = descriptionSettings.descriptionSwitch)
            Switch(
                checked = state.isEnabled,
                onCheckedChange = {
                    onEvent(BluetoothEvent.UpdateEnabled(it))
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.mask,
            onValueChange = { onEvent(BluetoothEvent.UpdateMask(it)) },
            label = { Text(descriptionSettings.hintTextField) },
            enabled = state.isEnabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@PreviewLightDark
@Composable
private fun SettingsContentPreview() = BluetoothTheme {
    val bluetoothDescriptionSettings = DescriptionSettings(
        title = stringResource(R.string.title_bluetooth),
        descriptionSwitch = stringResource(R.string.description_switch_bluetooth),
        hintTextField = stringResource(R.string.hint_text_bluetooth)
    )
    Surface {
        WirelessNetworkSettings(
            state = WirelessBluetoothMask(),
            onEvent = {},
            descriptionSettings = bluetoothDescriptionSettings,
        )
    }
}