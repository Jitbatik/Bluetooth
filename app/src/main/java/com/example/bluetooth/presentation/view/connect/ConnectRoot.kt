package com.example.bluetooth.presentation.view.connect

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.R
import com.example.bluetooth.model.BluetoothScreenType
import com.example.bluetooth.presentation.contracts.EnableBluetoothContract
import com.example.bluetooth.presentation.contracts.EnableLocationContract
import com.example.bluetooth.presentation.view.connect.components.enable.DataConfigurationPrompt
import com.example.bluetooth.presentation.view.connect.components.permission.BtPermissionNotProvidedBox
import com.example.bluetooth.presentation.view.connect.components.scanner.ScannerBox


@Composable
fun ConnectRoot(
    viewModel: ConnectViewModel = viewModel(),
) {
    val screenUiState by viewModel.connectContainerUiState.collectAsState()

    Connect(
        screenUiState = screenUiState,
        onEvents = viewModel::onEvents
    )
}

@Composable
private fun Connect(
    screenUiState: ConnectUiState,
    onEvents: (ConnectEvents) -> Unit,
) {
    val context = LocalContext.current

    val requiredPermissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        requiredPermissions.addAll(
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    } else {
        requiredPermissions.addAll(
            listOf(
                //12(API 31)+
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

    var hasBluetoothPermission by remember {
        mutableStateOf(
            requiredPermissions.all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PermissionChecker.PERMISSION_GRANTED
            }
        )
    }

    val screenType by remember(
        hasBluetoothPermission,
        screenUiState.isBluetoothEnabled,
        screenUiState.isLocationEnable
    ) {
        derivedStateOf {
            when {
                hasBluetoothPermission && screenUiState.isBluetoothEnabled && screenUiState.isLocationEnable -> BluetoothScreenType.BLUETOOTH_PERMISSION_GRANTED
                hasBluetoothPermission && !screenUiState.isBluetoothEnabled && screenUiState.isLocationEnable -> BluetoothScreenType.BLUETOOTH_NOT_ENABLED
                hasBluetoothPermission && !screenUiState.isBluetoothEnabled && !screenUiState.isLocationEnable -> BluetoothScreenType.BLUETOOTH_NOT_ENABLED
                hasBluetoothPermission && screenUiState.isBluetoothEnabled && !screenUiState.isLocationEnable -> BluetoothScreenType.LOCATION_NOT_ENABLED
                else -> BluetoothScreenType.BLUETOOTH_PERMISSION_DENIED
            }
        }
    }

    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = EnableBluetoothContract(),
        onResult = { _ -> }
    )
    val locationLauncher = rememberLauncherForActivityResult(
        contract = EnableLocationContract(context),
        onResult = { _ -> }
    )

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
                    ScannerBox(
                        deviceList = screenUiState.devices,
                        connectionState = screenUiState.connectionState,
                        isScanning = screenUiState.isScanning,
                        onEvents = onEvents,
                    )
                }

                BluetoothScreenType.BLUETOOTH_NOT_ENABLED -> {
                    DataConfigurationPrompt(
                        title = stringResource(id = R.string.bluetooth_not_enable_title),
                        description = stringResource(id = R.string.bluetooth_not_enable_desc),
                        actionButtonText = stringResource(id = R.string.enable_bluetooth_button_text),
                        launcher = { bluetoothLauncher.launch(Unit) },
                        modifier = Modifier.padding(12.dp)
                    )
                }

                BluetoothScreenType.BLUETOOTH_PERMISSION_DENIED -> {
                    BtPermissionNotProvidedBox(
                        requiredPermissions = requiredPermissions,
                        onPermissionChanged = { hasBluetoothPermission = it },
                        modifier = Modifier.padding(12.dp)
                    )
                }

                BluetoothScreenType.LOCATION_NOT_ENABLED -> {
                    DataConfigurationPrompt(
                        title = stringResource(id = R.string.location_not_enable_title),
                        description = stringResource(id = R.string.location_not_enable_desc),
                        actionButtonText = stringResource(id = R.string.location_bluetooth_button_text),
                        launcher = { locationLauncher.launch(Unit) },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}