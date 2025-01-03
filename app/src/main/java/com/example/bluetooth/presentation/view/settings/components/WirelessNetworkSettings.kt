package com.example.bluetooth.presentation.view.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.data.utils.SettingsManager
import com.example.bluetooth.model.DescriptionSettings
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun WirelessNetworkSettings(descriptionSettings: DescriptionSettings) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    var isChecked by remember { mutableStateOf(settingsManager.isEnabledChecked()) }
    var wirelessNetworkMask by remember { mutableStateOf(settingsManager.getBluetoothMask()) }
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_settings),
                contentDescription = "Settings Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = descriptionSettings.title,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Medium
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = descriptionSettings.descriptionSwitch)
                Switch(
                    checked = isChecked,
                    onCheckedChange = {
                        settingsManager.saveEnabledChecked(it)
                        isChecked = it
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = wirelessNetworkMask,
                onValueChange = { mask ->
                    settingsManager.saveBluetoothMask(mask)
                    wirelessNetworkMask = mask
                },
                label = { Text(descriptionSettings.hintTextField) },
                enabled = isChecked,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
        WirelessNetworkSettings(descriptionSettings = bluetoothDescriptionSettings)
    }
}