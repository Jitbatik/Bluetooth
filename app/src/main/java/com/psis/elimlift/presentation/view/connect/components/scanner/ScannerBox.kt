package com.psis.elimlift.presentation.view.connect.components.scanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.psis.elimlift.model.BluetoothDevice
import com.psis.elimlift.model.ConnectionState
import com.psis.elimlift.presentation.view.connect.ConnectEvents
import com.psis.elimlift.ui.theme.BluetoothTheme


@Composable
fun ScannerBox(
    deviceList: List<BluetoothDevice>,
    connectionState: ConnectionState,
    isScanning: Boolean,
    onEvents: (ConnectEvents) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            items(deviceList) { device ->
                DeviceCard(bluetoothDevice = device,
                    connectionState = connectionState,
                    onConnect = { selectedDevice ->
                        onEvents(ConnectEvents.ConnectToDevice(selectedDevice))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        ScanButton(
            isScanning = isScanning,
            onEvents = onEvents,
        )
    }
}


private data class ScannerBoxData(
    val deviceList: List<BluetoothDevice>,
    val connectionState: ConnectionState,
    val isScanning: Boolean,
)

private class ScannerBoxPreviewParameterProvider :
    PreviewParameterProvider<ScannerBoxData> {

    private fun createDevice(name: String, address: String) = BluetoothDevice(name, address)

    private val bluetoothDevices = listOf(
        createDevice("Device 1", "00:11:22:33:44:55"),
        createDevice("Device 2", "00:11:22:33:44:66")
    )

    override val values = sequenceOf(
        ScannerBoxData(
            deviceList = bluetoothDevices,
            connectionState = ConnectionState.Connecting(bluetoothDevices[0]),
            isScanning = true,
        ),
        ScannerBoxData(
            deviceList = bluetoothDevices,
            connectionState = ConnectionState.Connected(bluetoothDevices[0]),
            isScanning = false,
        ),
        ScannerBoxData(
            deviceList = bluetoothDevices,
            connectionState = ConnectionState.Disconnected(),
            isScanning = true,
        ),
        ScannerBoxData(
            deviceList = bluetoothDevices,
            connectionState = ConnectionState.Error(
                device = bluetoothDevices[1],
                message = "Connection failed"
            ),
            isScanning = false,
        )
    )
}

@PreviewLightDark
@Composable
private fun ScannerBoxPreview(
    @PreviewParameter(ScannerBoxPreviewParameterProvider::class)
    data: ScannerBoxData,
) = BluetoothTheme {
    Surface {
        ScannerBox(
            deviceList = data.deviceList,
            connectionState = data.connectionState,
            isScanning = data.isScanning,
            onEvents = {},
        )
    }
}