package com.example.bluetooth.presentation.view.connectcontainer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bluetooth.ConnectViewModel
import com.example.bluetooth.R
import com.example.domain.model.BluetoothDevice

@Composable
fun ScannerBox(
    viewModel: ConnectViewModel,
    deviceList:List<BluetoothDevice>
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
                DeviceCard(bluetoothDevice = device, viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Button(
            onClick = { viewModel.findToDevice() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(text = stringResource(R.string.scanner_box_button_title))
        }
    }
}


//@PreviewLightDark
//@Composable
//fun ScannerBoxPreview() = BluetoothTheme {
//
//    Surface {
//        ScannerBox(
//            viewModel = mockViewModel,
//            deviceList = listOf(BluetoothDevice(name = "Fake_Device", address = "12:34:56:78:90:AB")),
//
//        )
//    }
//}