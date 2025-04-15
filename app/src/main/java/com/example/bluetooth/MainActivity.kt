package com.example.bluetooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bluetooth.presentation.navigation.MainScreen
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.bluetooth.ui.theme.BluetoothTheme
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