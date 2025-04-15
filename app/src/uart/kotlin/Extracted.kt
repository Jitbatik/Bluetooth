import androidx.compose.runtime.Composable
import com.example.bluetooth.presentation.view.connect.ConnectRoot
import com.example.bluetooth.presentation.view.home.HomeRoot
import com.example.bluetooth.presentation.view.settings.SettingsRoot

@Composable
fun Extracted(currentRoute: String) {
    when (currentRoute) {
        NavigationItem.Home.route -> HomeRoot()
        NavigationItem.Connect.route -> ConnectRoot()
        NavigationItem.Settings.route -> SettingsRoot()
    }
}