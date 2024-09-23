package com.example.bluetooth.presentation.view.connectcontainer.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun CustomDialog(
    textProvider: TextProvider,
    openDialog: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        openAppSettings(context = context)
                    }
                ) {
                    Text(textProvider.buttonText, fontSize = 22.sp)
                }
            },
            title = {
                Text(
                    text = textProvider.title,
                )
            },
            text = {
                Text(
                    text = textProvider.description
                )
            },
            modifier = modifier
        )
    }
}


@PreviewLightDark
@Composable
private fun CustomDialogPreview() = BluetoothTheme {
    Surface {
        CustomDialog(
            textProvider = TextPermissionProvider(),
            openDialog = remember { mutableStateOf(true) },
            modifier = Modifier
        )
    }
}
