package com.psis.elimlift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.psis.elimlift.presentation.navigation.MainScreen
import com.psis.elimlift.presentation.navigation.NavigationStateHolder
import com.psis.elimlift.ui.theme.BluetoothTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigationStateHolder: NavigationStateHolder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothTheme {
                MainScreen(navigationStateHolder)
            }
        }
    }
}