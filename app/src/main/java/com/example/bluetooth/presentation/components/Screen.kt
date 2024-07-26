package com.example.bluetooth.presentation.components

sealed class Screen(val title: String) {
    object Home : Screen("Home")
    object Connect : Screen("Connect")
    object Settings : Screen("Settings")
}