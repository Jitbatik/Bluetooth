package com.example.bluetooth.presentation.view.connectcontainer

import android.Manifest
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.model.BluetoothScreenType
import com.example.bluetooth.presentation.view.connectcontainer.permission.BtPermissionNotProvidedBox
import com.example.bluetooth.presentation.view.connectcontainer.scanner.ScannerBox


@Composable
fun ConnectContainer(
    viewModel: ConnectViewModel = viewModel()
) {
    val context = LocalContext.current

    val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsState()

    var hasBluetoothPermission by remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_SCAN
                ) == PermissionChecker.PERMISSION_GRANTED
            )
        else {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PermissionChecker.PERMISSION_GRANTED
            )
        }
    }

    val screenType by remember(hasBluetoothPermission, isBluetoothEnabled) {
        derivedStateOf {
            when {
                hasBluetoothPermission && isBluetoothEnabled -> BluetoothScreenType.BLUETOOTH_PERMISSION_GRANTED
                hasBluetoothPermission && !isBluetoothEnabled -> BluetoothScreenType.BLUETOOTH_NOT_ENABLED
                else -> BluetoothScreenType.BLUETOOTH_PERMISSION_DENIED
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = screenType,
            label = "Screen Type Transition"
        ) { mode ->
            when (mode) {
                BluetoothScreenType.BLUETOOTH_PERMISSION_GRANTED -> {
                    ScannerBox(viewModel)
                }

                BluetoothScreenType.BLUETOOTH_NOT_ENABLED -> {
                    BTNotEnabledBox(
                        modifier = Modifier.padding(12.dp)
                    )
                }

                BluetoothScreenType.BLUETOOTH_PERMISSION_DENIED -> {
                    BtPermissionNotProvidedBox(
                        onPermissionChanged = { hasBluetoothPermission = it },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

//@PreviewLightDark
//@Composable
//fun DeviceCardPreview() = BluetoothTheme {
//    Surface {
//        DeviceCard(
//            bluetoothDevice = BluetoothDevice(name = "Fake_Device", address = "12.12.21"),
//            viewModel = viewModel()
//        )
//    }
//}