package com.example.bluetooth.presentation.view.connect.components.scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.bluetooth.model.BluetoothDevice
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun DeviceCard(
    bluetoothDevice: BluetoothDevice,
    isConnected: Boolean,
    onConnect: (BluetoothDevice) -> Unit,
) {
    val cardColor = if (isConnected) Color.Green else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isConnected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onConnect(bluetoothDevice) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = bluetoothDevice.name, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = bluetoothDevice.address, color = textColor)
        }
    }
}

private data class DeviceCardData(
    val device: BluetoothDevice,
    val isConnected: Boolean,
)

private class DeviceCardPreviewParameterProvider :
    PreviewParameterProvider<DeviceCardData> {
    override val values = sequenceOf(
        DeviceCardData(
            device = BluetoothDevice(name = "Device A", address = "12:34:56:78:90:AB"),
            isConnected = true
        ),
        DeviceCardData(
            device = BluetoothDevice(name = "Device B", address = "98:76:54:32:10:FE"),
            isConnected = false
        ),
    )
}

@PreviewLightDark
@Composable
private fun DeviceCardPreview(
    @PreviewParameter(DeviceCardPreviewParameterProvider::class)
    deviceState: DeviceCardData,
) = BluetoothTheme {
    Surface {

        DeviceCard(
            bluetoothDevice = deviceState.device,
            isConnected = deviceState.isConnected,
            onConnect = {}
        )
    }
}