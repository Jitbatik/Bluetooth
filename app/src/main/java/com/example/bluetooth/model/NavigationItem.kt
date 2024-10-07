package com.example.bluetooth.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.bluetooth.R
import com.example.bluetooth.presentation.screen.Screen

enum class NavigationItem(
    val screen: Screen,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {
    Home(
        screen = Screen.Home,
        icon = R.drawable.icon_home,
        title = R.string.navigation_item_home
    ),
    Connect(
        screen = Screen.Connect,
        icon = R.drawable.icon_bluetooth,
        title = R.string.navigation_item_connect
    ),
    Settings(
        screen = Screen.Settings,
        icon = R.drawable.icon_settings,
        title = R.string.navigation_item_settings
    )
}

