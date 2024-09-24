package com.example.bluetooth.presentation.view.connectcontainer.scanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    colors: ButtonColors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
    ),
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {


        AnimatedButton(
            modifier = modifier,
            colors = colors,
            shadowColor = Color.DarkGray,
            shadowBottomOffset = 5f,
            buttonHeight = 50f,
            shape = RoundedCornerShape(18.dp),
            onClick = {
                if (isScanning) {
                    onEvent(ConnectContainerEvents.StopScan)
                } else {
                    onEvent(ConnectContainerEvents.StartScan)
                }
            },
        ) {
            AnimatedContent(
                targetState = isScanning,
                label = "Start or stop scan button",
                transitionSpec = {
                    if (targetState) {
                        slideInVertically { height -> -height } + fadeIn(initialAlpha = .25f) togetherWith
                                slideOutVertically { height -> height } + fadeOut(targetAlpha = .25f)
                    } else {
                        slideInVertically { height -> height } + fadeIn(initialAlpha = .25f) togetherWith
                                slideOutVertically { height -> -height } + fadeOut(targetAlpha = .25f)
                    }
                },
            ) { scanning ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (scanning) {
                        Text(
                            text = "Stop",
                            style = style
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.scanner_box_button_title),
                            style = style
                        )
                    }
                }
            }
        }
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