package ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import navigation.NavigationItem

@Composable
fun ParametersDashboardActions(
    navigationStateHolder: NavigationStateHolder
) {
    IconButton(
        onClick = { navigationStateHolder.setCurrentScreen(NavigationItem.Settings) }
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Configure Parameters"
        )
    }
} 