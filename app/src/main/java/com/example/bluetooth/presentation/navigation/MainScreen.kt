package com.example.bluetooth.presentation.navigation

import navigation.NavigationItem
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.bluetooth.model.CustomDrawerState
import com.example.bluetooth.model.isOpened
import com.example.bluetooth.presentation.Content
import kotlin.math.roundToInt

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun MainScreen(
    navigationStateHolder: NavigationStateHolder
) {
    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    val selectedNavigationItem by navigationStateHolder.currentScreen.collectAsState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density

    val screenWidth = remember(configuration.screenWidthDp, density) {
        (configuration.screenWidthDp * density).roundToInt()
    }
    val offsetValue = remember(screenWidth) { (screenWidth / 4.5).dp }

    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp,
        label = "Animated Offset"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f,
        label = "Animated Scale"
    )

    BackHandler(enabled = drawerState.isOpened()) {
        drawerState = CustomDrawerState.Closed
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        Drawer(
            selectedRoute = selectedNavigationItem.route,
            onNavigationItemClick = { newRoute ->
                val newScreen = NavigationItem.fromRoute(newRoute) ?: NavigationItem.Home
                navigationStateHolder.setCurrentScreen(newScreen)
                drawerState = CustomDrawerState.Closed
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed }
        )
        Content(
            modifier = Modifier
                .offset(x = animatedOffset)
                .scale(scale = animatedScale),
            drawerState = drawerState,
            onDrawerClick = { drawerState = it },
            currentRoute = selectedNavigationItem.route,
            navigationStateHolder = navigationStateHolder
        )
    }
}
