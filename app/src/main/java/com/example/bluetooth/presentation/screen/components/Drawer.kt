package com.example.bluetooth.presentation.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.model.NavigationItem
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun Drawer(
    selectedNavigationItem: NavigationItem,
    onNavigationItemClick: (NavigationItem) -> Unit,
    onCloseClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.6f)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back Arrow Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier.size(100.dp),
            painter = painterResource(id = R.drawable.icons_connect),
            contentDescription = "Zodiac Image"
        )
        Spacer(modifier = Modifier.height(40.dp))
        NavigationItem.entries.toTypedArray().take(2).forEach { navigationItem ->
            NavigationItemView(
                navigationItem = navigationItem,
                selected = navigationItem == selectedNavigationItem,
                onClick = { onNavigationItemClick(navigationItem) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        NavigationItem.entries.toTypedArray().takeLast(1).forEach { navigationItem ->
            NavigationItemView(
                navigationItem = navigationItem,
                selected = navigationItem == selectedNavigationItem,
                onClick = { onNavigationItemClick(navigationItem) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}


private class DrawerPreviewParameterProvider : PreviewParameterProvider<NavigationItem> {
    override val values = sequenceOf(
        NavigationItem.Home,
        NavigationItem.Connect,
        NavigationItem.Settings
    )
}

@PreviewLightDark
@Composable
fun DrawerPreview(
    @PreviewParameter(DrawerPreviewParameterProvider::class) item: NavigationItem
) = BluetoothTheme {
    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            Drawer(
                selectedNavigationItem = item,
                onNavigationItemClick = { },
                onCloseClick = { }
            )
        }

    }
}