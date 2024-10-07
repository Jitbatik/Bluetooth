package com.example.bluetooth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.bluetooth.model.CustomDrawerState
import com.example.bluetooth.model.opposite
import com.example.bluetooth.presentation.screen.Screen
import com.example.bluetooth.presentation.view.connect.ConnectRoot
import com.example.bluetooth.presentation.view.home.HomeRoot
import com.example.bluetooth.presentation.view.settings.SettingsRoot
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun Content(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit,
    screen: Screen,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = screen.title,
                onDrawerClick = { onDrawerClick(drawerState.opposite()) }
            )
        },
        content = { paddingValues ->
            Box(modifier = modifier.padding(paddingValues)) {
                when (screen) {
                    is Screen.Home -> HomeRoot()
                    is Screen.Connect -> ConnectRoot()
                    is Screen.Settings -> SettingsRoot()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onDrawerClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onDrawerClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Icon"
                )
            }
        }
    )
}


private class AppTopBarPreviewParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf(
        "Home", "Connect", "Settings"
    )
}

@PreviewLightDark
@Composable
private fun AppTopBarPreview(
    @PreviewParameter(AppTopBarPreviewParameterProvider::class) title: String,
) = BluetoothTheme {
    AppTopBar(
        title = title,
        onDrawerClick = {}
    )
}

private class ContentPreviewParameterProvider : PreviewParameterProvider<Screen> {
    override val values = sequenceOf(
        Screen.Home, Screen.Connect, Screen.Settings
    )
}

@PreviewLightDark
@Composable
private fun ContentPreview(
    @PreviewParameter(ContentPreviewParameterProvider::class) screen: Screen
) = BluetoothTheme {
    Content(
        drawerState = CustomDrawerState.Opened,
        onDrawerClick = { },
        screen = screen,
    )
}