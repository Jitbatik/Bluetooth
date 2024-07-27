package com.example.bluetooth.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluetooth.ConnectViewModel
import com.example.domain.model.Device

@Preview(showSystemUi = true)
@Composable
fun ConnectContent(viewModel: ConnectViewModel = hiltViewModel()) {
    val deviceList by viewModel.mediatorLiveData.observeAsState(emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                items(deviceList) { device ->
                    DeviceCard(device = device, viewModel = viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Button(
                onClick = { viewModel.findToDevice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(text = "Сканировать")
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    viewModel: ConnectViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { viewModel.handlerConnectionToDevice(device = device) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = device.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = device.address)
        }
    }
}