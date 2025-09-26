package com.example.bluetooth.presentation.view.connect.components.scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.bluetooth.model.BluetoothDevice
import com.example.bluetooth.model.ConnectionState
import com.example.bluetooth.ui.theme.BluetoothTheme


@Composable
fun DeviceCard(
    bluetoothDevice: BluetoothDevice,
    connectionState: ConnectionState,
    onConnect: (BluetoothDevice) -> Unit,
) {
    val isThisDevice = when (connectionState) {
        is ConnectionState.Connected -> connectionState.device.address == bluetoothDevice.address
        is ConnectionState.Connecting -> connectionState.device.address == bluetoothDevice.address
        is ConnectionState.Error -> connectionState.device?.address == bluetoothDevice.address
        else -> false
    }

    val cardColor = when {
        connectionState is ConnectionState.Connected && isThisDevice -> Color.Green
        connectionState is ConnectionState.Error && isThisDevice -> Color.Red
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val showProgress = connectionState is ConnectionState.Connecting && isThisDevice
    val textColor = if (cardColor == Color.Green || cardColor == Color.Red)
        Color.Black
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onConnect(bluetoothDevice) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = bluetoothDevice.name,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bluetoothDevice.address,
                    color = textColor
                )
            }

            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = bluetoothDevice.rssi.toString(),
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "dBm",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}


private data class DeviceCardPreviewData(
    val device: BluetoothDevice,
    val connectionState: ConnectionState
)

private class DeviceCardPreviewParameterProvider :
    PreviewParameterProvider<DeviceCardPreviewData> {

    private val deviceA =
        BluetoothDevice(name = "Device A", address = "12:34:56:78:90:AB", rssi = -40)
    private val deviceB =
        BluetoothDevice(name = "Device B", address = "98:76:54:32:10:FE", rssi = -60)
    private val deviceC =
        BluetoothDevice(name = "Device C", address = "11:22:33:44:55:66", rssi = -50)
    private val deviceD =
        BluetoothDevice(name = "Device D", address = "AA:BB:CC:DD:EE:FF", rssi = -70)

    override val values = sequenceOf(
        DeviceCardPreviewData(
            device = deviceA,
            connectionState = ConnectionState.Connected(deviceA)
        ),
        DeviceCardPreviewData(
            device = deviceB,
            connectionState = ConnectionState.Disconnected()
        ),
        DeviceCardPreviewData(
            device = deviceC,
            connectionState = ConnectionState.Connecting(deviceC)
        ),
        DeviceCardPreviewData(
            device = deviceD,
            connectionState = ConnectionState.Error(deviceD, "Connection failed")
        )
    )
}


@PreviewLightDark
@Composable
private fun DeviceCardPreview(
    @PreviewParameter(DeviceCardPreviewParameterProvider::class)
    deviceState: DeviceCardPreviewData,
) = BluetoothTheme {
    Surface {

        DeviceCard(
            bluetoothDevice = deviceState.device,
            connectionState = deviceState.connectionState,
            onConnect = {}
        )
    }
}