package com.example.bluetooth.presentation.view.connect.components.scanner

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
import com.example.bluetooth.presentation.view.connect.ConnectEvents
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.domain.model.BluetoothDevice

@Composable
fun ScannerBox(
    deviceList: List<BluetoothDevice>,
    connectedDevice: BluetoothDevice?,
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
                    isConnected = device == connectedDevice,
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
    val connectedDevice: BluetoothDevice?,
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
            connectedDevice = bluetoothDevices[0],
            isScanning = false,
        ),
        ScannerBoxData(
            deviceList = bluetoothDevices,
            connectedDevice = null,
            isScanning = true,
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
            connectedDevice = data.connectedDevice,
            isScanning = data.isScanning,
            onEvents = {},
        )
    }
}