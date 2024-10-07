package com.example.bluetooth.presentation.screen

sealed class Screen(val title: String) {
    data object Home : Screen("Home")
    data object Connect : Screen("Connect")
    data object Settings : Screen("Settings")
}