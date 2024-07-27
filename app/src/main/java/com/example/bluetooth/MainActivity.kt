package com.example.bluetooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.bluetooth.ui.theme.BluetoothTheme
import com.example.nativeappjetpaccompouse.presentation.screen.MainScreen
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
            BluetoothTheme {

            }
        }
    }
}

@Preview (showSystemUi = true)
@Composable
fun MyApp() {
    BluetoothTheme {
        MainScreen()
    }
}