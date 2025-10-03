package com.psis.elimlift.presentation.view.connect.components.scanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.psis.elimlift.R
import com.psis.elimlift.presentation.view.connect.ConnectEvents
import com.psis.elimlift.presentation.view.connect.components.KeyboardButton
import com.psis.elimlift.ui.theme.BluetoothTheme

@Composable
fun ScanButton(
    isScanning: Boolean,
    onEvents: (ConnectEvents) -> Unit,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    colors: ButtonColors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
    ),
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        KeyboardButton(
            colors = colors,
            shadowColor = Color.DarkGray,
            shadowBottomOffset = 12f,
            buttonHeight = 50f,
            isPressed = isScanning,
            shape = RoundedCornerShape(18.dp),
            onClick = {
                if (isScanning) {
                    onEvents(ConnectEvents.StopScan)
                } else {
                    onEvents(ConnectEvents.StartScan)
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

private data class ScanButtonData(
    val isScanning: Boolean,
)

private class ScanButtonPreviewParameterProvider : PreviewParameterProvider<ScanButtonData> {
    override val values = sequenceOf(
        ScanButtonData(
            isScanning = true,
        ),
        ScanButtonData(
            isScanning = false,
        ),
    )
}

@PreviewLightDark
@Composable
private fun ScanButtonPreview(
    @PreviewParameter(ScanButtonPreviewParameterProvider::class)
    data: ScanButtonData,
) = BluetoothTheme {
    Surface {
        ScanButton(
            isScanning = data.isScanning,
            onEvents = {},
        )
    }
}