package com.example.bluetooth.presentation.view.connectcontainer.scanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.presentation.view.connectcontainer.ConnectContainerEvents
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun AnimatedScanButton(
    isScanning: Boolean,
    modifier: Modifier = Modifier,
    onEvent: (ConnectContainerEvents) -> Unit,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    colors: ButtonColors = ButtonDefaults
        .textButtonColors(contentColor = MaterialTheme.colorScheme.inverseOnSurface),
) {
    AnimatedContent(
        targetState = !isScanning,
        modifier = modifier,
        label = "Start or stop scan button",
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { height -> -height } + fadeIn(initialAlpha = .25f) togetherWith
                        slideOutVertically { height -> height } + fadeOut(targetAlpha = .25f)
            } else {
                slideInVertically { height -> height } + fadeIn(initialAlpha = .25f) togetherWith
                        slideOutVertically { height -> -height } + fadeOut(targetAlpha = .25f)
            }
        },
    ) { normal ->
        if (normal)
            AnimatedButton(
                buttonColor = Color.Unspecified,
                buttonContentColor = Color.Unspecified,
                shadowColor = Color.DarkGray,
                shadowBottomOffset = 12f,
                buttonHeight = 50f,
                shape = RoundedCornerShape(18.dp),
                onClick = { onEvent(ConnectContainerEvents.StartScan) }
            ) {
                Text(
                    text = stringResource(id = R.string.scanner_box_button_title),
                    style = style
                )
            }
//            TextButton(onClick = { onEvent(ConnectContainerEvents.StartScan) }, colors = colors) {
//                Text(
//                    text = stringResource(id = R.string.scanner_box_button_title),
//                    style = style
//                )
//            }
        else AnimatedButton(
            buttonColor = Color.Unspecified,
            buttonContentColor = Color.Unspecified,
            shadowColor = Color.DarkGray,
            shadowBottomOffset = 12f,
            buttonHeight = 50f,
            shape = RoundedCornerShape(18.dp),
            onClick = { onEvent(ConnectContainerEvents.StopScan) }
        ) {
            Text(
                text = "Stop",
                style = style
            )
        }
//            TextButton(onClick = { onEvent(ConnectContainerEvents.StopScan) }, colors = colors) {
//            Text(
//                text = "Stop",
//                style = style
//            )
//        }
    }
}


@PreviewLightDark
@Composable
fun ScannerBoxPreview(
//    @PreviewParameter(ScannerBoxPreviewParameterProvider::class)
//    data: Pair<List<BluetoothDevice>, BluetoothDevice?>,
) = BluetoothTheme {
    Surface {
        //val (deviceList, connectedDevice) = data
        AnimatedScanButton(
            isScanning = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.inversePrimary,
                    shape = RoundedCornerShape(18.dp)
                ),
            onEvent = {},
        )
    }
}