package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.DateTime
import ui.components.ParameterItem

@Composable
fun ParametersRoot(
    viewModel: ElevatorParametersViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    Parameters(
        state = state
    )
}

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
            text = formatDateTime(state.parametersGroup.time),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.parametersGroup.data) { parameter ->
                ParameterItem(
                    parameter = parameter,
                    chartParameters = state.chartParameters[parameter.id] ?: ChartParameters(),
                    onEvents = state.onEvents
                )
            }
        }
    }
}


private fun formatDateTime(time: DateTime): String {
    return "${time.year}-${time.month}-${time.day} ${time.hour}:${time.minute}:${time.second}"
}
//todo сделать Preview и отрефакторить