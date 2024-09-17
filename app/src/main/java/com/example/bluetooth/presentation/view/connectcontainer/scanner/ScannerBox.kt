package com.example.bluetooth.presentation.view.connectcontainer.scanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.presentation.view.connectcontainer.BTDevicesScreenEvents
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.domain.model.BluetoothDevice

@Composable
fun ScannerBox(
    deviceList: List<BluetoothDevice>,
    connectedDevice: BluetoothDevice?,
    onEvent: (BTDevicesScreenEvents) -> Unit,
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
                        onEvent(BTDevicesScreenEvents.ConnectToDevice(selectedDevice))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Button(
            onClick = { onEvent(BTDevicesScreenEvents.StartScan) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(text = stringResource(R.string.scanner_box_button_title))
        }
    }
}

class ScannerBoxPreviewParameterProvider :
    PreviewParameterProvider<Pair<List<BluetoothDevice>, BluetoothDevice?>> {

    override val values = sequenceOf(
        Pair(
            listOf(
                BluetoothDevice("Device 1", "00:11:22:33:44:55"),
                BluetoothDevice("Device 2", "00:11:22:33:44:66")
            ),
            BluetoothDevice("Device 1", "00:11:22:33:44:55")
        ),
        Pair(
            listOf(
                BluetoothDevice("Device 1", "00:11:22:33:44:55"),
                BluetoothDevice("Device 2", "00:11:22:33:44:66")
            ),
            null
        )
    )
}

@PreviewLightDark
@Composable
fun ScannerBoxPreview(
    @PreviewParameter(ScannerBoxPreviewParameterProvider::class)
    data: Pair<List<BluetoothDevice>, BluetoothDevice?>
) = BluetoothTheme {
    Surface {
        val (deviceList, connectedDevice) = data
        ScannerBox(
            deviceList = deviceList,
            connectedDevice = connectedDevice,
            onEvent = {},
        )
    }
}