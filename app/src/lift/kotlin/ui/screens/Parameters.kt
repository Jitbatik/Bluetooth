package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.presentation.ElevatorParametersViewModel
import com.example.bluetooth.presentation.ParametersState
import ui.components.LineCharts
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ParametersRoot(
    viewModel: ElevatorParametersViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    Parameters(state = state)
}

// todo сделать Нужно добавить что-то вроде настройки какие графики отображать а какие игнорировать
@Composable
fun Parameters(
    state: ParametersState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = formatDateTime(
                timeSeconds = state.parametersGroup.lastOrNull()?.timeStamp ?: 0,
                timeMilliseconds = state.parametersGroup.lastOrNull()?.timeMilliseconds ?: 0,
            ),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        LineCharts(
            parameters = state.parametersGroup,
            chartParameters = state.chartParameters,
            onEvents = state.onEvents,
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}


private fun formatDateTime(timeSeconds: Long, timeMilliseconds: Int): String {
    val instant = Instant.ofEpochMilli(timeSeconds * 1000 + timeMilliseconds)
    return DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}
//todo сделать Preview и отрефакторить