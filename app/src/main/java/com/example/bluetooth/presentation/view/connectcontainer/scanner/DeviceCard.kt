package com.example.bluetooth.presentation.view.connectcontainer.scanner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.BluetoothDevice

@Composable
fun DeviceCard(
    bluetoothDevice: BluetoothDevice,
    onConnect: (BluetoothDevice) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onConnect(bluetoothDevice) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = bluetoothDevice.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = bluetoothDevice.address)
        }
    }
}

//@PreviewLightDark
//@Composable
//fun DeviceCardPreview() = BluetoothTheme {
//    Surface {
//        DeviceCard(
//            bluetoothDevice = BluetoothDevice(name = "Fake_Device", address = "12.12.21"),
//            onConnect = {}
//        )
//    }
//}