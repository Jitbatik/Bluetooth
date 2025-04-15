package navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.bluetooth.R


enum class NavigationItem(
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int
) {
    Home(
        route = "home",
        icon = R.drawable.icon_home,
        title = R.string.navigation_item_home
    ),
    Parameters(
        route = "parameters",
        icon = R.drawable.icon_settings,
        title = R.string.navigation_item_parameters
    ),
    Connect(
        route = "connect",
        icon = R.drawable.icon_bluetooth,
        title = R.string.navigation_item_connect
    ),

    Settings(
        route = "settings",
        icon = R.drawable.icon_settings,
        title = R.string.navigation_item_settings
    );

    companion object {
        fun fromRoute(route: String?): NavigationItem? = entries.find { it.route == route }
    }
}


