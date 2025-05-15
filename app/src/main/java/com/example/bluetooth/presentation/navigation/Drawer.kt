package com.example.bluetooth.presentation.navigation

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.bluetooth.R
import com.example.bluetooth.ui.theme.BluetoothTheme
import navigation.NavigationItem

@Composable
fun Drawer(
    selectedRoute: String,
    onNavigationItemClick: (String) -> Unit,
    onCloseClick: () -> Unit,
) {
    val items = remember { NavigationItem.entries.toList() }

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

        items.dropLast(1).forEach { navigationItem ->
            NavigationItemView(
                navigationItem = navigationItem,
                selected = navigationItem.route == selectedRoute,
                onClick = { onNavigationItemClick(navigationItem.route) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        items.lastOrNull()?.let { lastItem ->
            NavigationItemView(
                navigationItem = lastItem,
                selected = lastItem.route == selectedRoute,
                onClick = { onNavigationItemClick(lastItem.route) }
            )
        }
    }
}



private class DrawerPreviewParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("home", "connect", "settings")
}

@PreviewLightDark
@Composable
fun DrawerPreview(
    @PreviewParameter(DrawerPreviewParameterProvider::class) item: String
) = BluetoothTheme {
    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            Drawer(
                selectedRoute = item,
                onNavigationItemClick = { },
                onCloseClick = { }
            )
        }
    }
}
