package override.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import override.navigation.NavigationItem

@Composable
fun Actions(
    navigationStateHolder: NavigationStateHolder,
    rssi: String = "",
    color: Color = Color.Unspecified
) {
    Text(
        text = rssi,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    IconButton(
        onClick = { navigationStateHolder.setCurrentScreen(NavigationItem.Settings) }
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Configure Parameters"
        )
    }
} 