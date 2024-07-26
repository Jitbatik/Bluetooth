package com.example.nativeappjetpaccompouse.presentation

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
import com.example.bluetooth.model.CustomDrawerState
import com.example.bluetooth.model.opposite
import com.example.bluetooth.presentation.components.Screen
import com.example.nativeappjetpaccompouse.presentation.view.ConnectContent
import com.example.nativeappjetpaccompouse.presentation.view.HomeContent
import com.example.nativeappjetpaccompouse.presentation.view.SettingsContent

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit,
    screen: Screen
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
                    is Screen.Home -> HomeContent()
                    is Screen.Connect -> ConnectContent()
                    is Screen.Settings -> SettingsContent()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onDrawerClick: () -> Unit
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
