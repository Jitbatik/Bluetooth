package com.example.bluetooth.presentation.view.connectcontainer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.presentation.contracts.EnableBluetoothContract
import com.example.bluetooth.ui.theme.BluetoothTheme
import androidx.activity.result.launch

@Composable
fun EnableBluetoothButton(
    onResults: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    val launcher = rememberLauncherForActivityResult(
        contract = EnableBluetoothContract(),
        onResult = onResults
    )

    Button(
        onClick = launcher::launch,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {

        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = stringResource(id = R.string.enable_bluetooth_button_text),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@PreviewLightDark
@Composable
private fun BTNotEnabledBoxPreview() = BluetoothTheme {
    Surface {
        EnableBluetoothButton(onResults = {})
    }
}