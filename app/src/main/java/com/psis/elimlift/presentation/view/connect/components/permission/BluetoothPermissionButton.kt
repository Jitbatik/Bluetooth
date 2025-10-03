package com.psis.elimlift.presentation.view.connect.components.permission

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
import com.psis.elimlift.ui.theme.BluetoothTheme


@Composable
fun BluetoothPermissionButton(
    modifier: Modifier = Modifier,
    onResults: (Boolean) -> Unit = {},
    requiredPermissions: List<String>,
) {
    val openDialog = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val hasBothPermission = requiredPermissions.all { perms[it] == true }

        if (hasBothPermission) {
            onResults(true)
        } else {
            openDialog.value = true
            onResults(false)
        }
    }

    if (openDialog.value) {
        CustomDialog(
            textProvider = TextPermissionProvider(),
            openDialog = openDialog,
            modifier = Modifier
        )
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
}

@PreviewLightDark
@Composable
private fun BluetoothPermissionButtonPreview() = BluetoothTheme {
    BluetoothPermissionButton(
        onResults = {},
        requiredPermissions = emptyList()
    )
}