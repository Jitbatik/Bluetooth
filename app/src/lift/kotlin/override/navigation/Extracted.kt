package override.navigation

import androidx.compose.runtime.Composable
import com.psis.elimlift.presentation.navigation.NavigationStateHolder
import com.psis.elimlift.presentation.view.connect.ConnectRoot
import com.psis.elimlift.presentation.view.home.HomeRoot
import com.psis.elimlift.presentation.view.settings.SettingsRoot
import com.psis.elimlift.presentation.view.parameters.ui.ParametersDashboardRoot

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