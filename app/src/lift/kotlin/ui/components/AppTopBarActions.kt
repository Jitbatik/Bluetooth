package ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import navigation.NavigationItem

// todo думаю из ProcessParametersFeatureCase нужно выделить логику
//  в отдельный класс который занимается обработкой  данных на основе данных из CSV конфига
//  Command тоже можно будет настроить из под CSV вот будет шикарно
// TODO тут будет настройка для управления отображаемыми сигналами
@Composable
fun AppTopBarActions(
    currentRoute: String,
) {
    if (currentRoute == NavigationItem.Parameters.route) {
        IconButton(onClick = { /* Action 1 */ }) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = "Build liner graph"
            )
        }
    }
}