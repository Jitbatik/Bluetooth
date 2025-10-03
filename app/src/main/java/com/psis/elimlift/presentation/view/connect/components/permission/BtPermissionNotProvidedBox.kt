package com.psis.elimlift.presentation.view.connect.components.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.psis.elimlift.R
import com.psis.elimlift.ui.theme.BluetoothTheme

@Composable
fun BtPermissionNotProvidedBox(
    requiredPermissions: List<String>,
    onPermissionChanged: (Boolean) -> Unit = {},
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.bluetooth_permission_not_found_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(id = R.string.bluetooth_permission_not_found_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        BluetoothPermissionButton(
            requiredPermissions = requiredPermissions,
            modifier = Modifier.padding(top = 8.dp),
            onResults = onPermissionChanged
        )
    }
}

@PreviewLightDark
@Composable
private fun BTPermissionNotProvidedPreview() = BluetoothTheme {
    Surface {
        BtPermissionNotProvidedBox(
            requiredPermissions = emptyList(),
            modifier = Modifier.padding(16.dp)
        )
    }
}