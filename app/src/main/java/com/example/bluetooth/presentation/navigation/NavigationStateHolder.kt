package com.example.bluetooth.presentation.navigation

import navigation.NavigationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationStateHolder @Inject constructor() {
    private val _currentScreen = MutableStateFlow(NavigationItem.Home)
    val currentScreen: StateFlow<NavigationItem> = _currentScreen

    fun setCurrentScreen(screen: NavigationItem) {
        _currentScreen.value = screen
    }
}
