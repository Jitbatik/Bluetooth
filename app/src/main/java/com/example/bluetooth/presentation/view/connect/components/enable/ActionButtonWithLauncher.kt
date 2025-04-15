package com.example.bluetooth.presentation.view.connect.components.enable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun ActionButtonWithLauncher(
    text: String,
    launcher: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    Button(
        onClick = launcher,
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding
    ) {
        Text(
            text = text,
            style = textStyle
        )
    }
}

@PreviewLightDark
@Composable
private fun ActionButtonWithLauncherPreview() = BluetoothTheme {
    Surface {
        ActionButtonWithLauncher(
            text = "Button",
            launcher = {}
        )
    }
}