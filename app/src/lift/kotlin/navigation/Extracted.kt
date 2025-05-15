package navigation

import androidx.compose.runtime.Composable
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.bluetooth.presentation.view.connect.ConnectRoot
import com.example.bluetooth.presentation.view.home.HomeRoot
import com.example.bluetooth.presentation.view.settings.SettingsRoot
import ui.screens.ParametersDashboardRoot

@Composable
fun Extracted(
    currentRoute: String,
    navigationStateHolder: NavigationStateHolder
) {
    when (currentRoute) {
        NavigationItem.Home.route -> HomeRoot()
        NavigationItem.Connect.route -> ConnectRoot()
        NavigationItem.Settings.route -> SettingsRoot()
        NavigationItem.ParametersDashboard.route -> ParametersDashboardRoot(navigationStateHolder = navigationStateHolder)
    }
}