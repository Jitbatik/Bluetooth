package com.example.bluetooth.presentation.view.connectcontainer.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun BluetoothPermissionButton(
    modifier: Modifier = Modifier,
    onResults: (Boolean) -> Unit = {},
) {

    val requiredPermissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
        requiredPermissions.addAll(
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    } else {
        requiredPermissions.addAll(
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION // for scan new device
            )
        )
    }

    val openDialog = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val hasBothPermission = requiredPermissions.all { perms[it] == true }
        onResults(hasBothPermission)
    }

    Button(
        onClick = {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = "Allow Permissions")
    }

    if (openDialog.value) {
        CustomDialog(
            textProvider = TextPermissionProvider(),
            openDialog = openDialog,
            modifier = Modifier
        )
    }
}


@PreviewLightDark
@Composable
private fun BluetoothPermissionButtonPreview() = BluetoothTheme {
    BluetoothPermissionButton(onResults = {})
}