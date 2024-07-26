package com.example.nativeappjetpaccompouse.presentation.view

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetooth.model.Device

@Preview(showSystemUi = true)
@Composable
fun ConnectContent() {

    val deviceList = listOf(
        Device("Device 1", "00:11:22:33:44:55"),
        Device("Device 2", "66:77:88:99:AA:BB")
        // Добавьте другие устройства по мере необходимости
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            //center
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                items(deviceList) { device ->
                    DeviceCard(device = device)
                    Spacer(modifier = Modifier.height(8.dp))
                }

            }
            Button(
                onClick = { /* Ваш обработчик нажатия */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) // Отступ снизу
            ) {
                Text(text = "Сканировать")
            }
        }

    }
}

@Composable
fun DeviceCard(device: Device) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /**/ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp) // Внутренние отступы карточки
        ) {
            Text(text = device.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp)) // Отступ между текстами
            Text(text = device.address)
        }
    }
}