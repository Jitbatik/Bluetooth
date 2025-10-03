package com.psis.elimlift.presentation.view.connect.components.enable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.psis.elimlift.ui.theme.BluetoothTheme

@Composable
fun DataConfigurationPrompt(
    title: String,
    description: String,
    actionButtonText: String,
    launcher: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        ActionButtonWithLauncher(
            text = actionButtonText,
            launcher = launcher,
            modifier = Modifier.fillMaxWidth(.75f)
        )
    }
}

@PreviewLightDark
@Composable
private fun DataConfigurationPromptPreview() = BluetoothTheme {
    Surface {
        DataConfigurationPrompt(
            title = "not_enable_title",
            description = "not_enable_desc",
            actionButtonText = "button_text",
            launcher = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}