package com.example.bluetooth.presentation

import override.navigation.Extracted
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.bluetooth.model.CustomDrawerState
import com.example.bluetooth.model.opposite
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.bluetooth.ui.theme.BluetoothTheme
import override.navigation.NavigationItem
import override.ui.Actions

@Composable
fun Content(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit,
    currentRoute: String,
    navigationStateHolder: NavigationStateHolder
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = NavigationItem.fromRoute(currentRoute)?.title?.let { stringResource(it) }
                    ?: "",
                onDrawerClick = { onDrawerClick(drawerState.opposite()) },
                actions = {
                    when (currentRoute) {
                        NavigationItem.ParametersDashboard.route -> {
                            Actions(navigationStateHolder = navigationStateHolder)
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Extracted(currentRoute, navigationStateHolder)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onDrawerClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onDrawerClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Icon"
                )
            }
        },
        actions = actions
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

private class ContentPreviewParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf(
        NavigationItem.Home.route,
        NavigationItem.Connect.route,
        NavigationItem.Settings.route
    )
}

@PreviewLightDark
@Composable
private fun ContentPreview(
    @PreviewParameter(ContentPreviewParameterProvider::class) route: String
) = BluetoothTheme {
    Content(
        drawerState = CustomDrawerState.Opened,
        onDrawerClick = { },
        currentRoute = route,
        navigationStateHolder = NavigationStateHolder()
    )
}