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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.R
import com.example.bluetooth.presentation.ElevatorParametersViewModel
import com.example.bluetooth.presentation.ParametersState
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.bluetooth.presentation.view.connect.components.enable.DataConfigurationPrompt
import com.example.transfer.domain.utils.DateTimeUtils
import navigation.NavigationItem
import ui.components.LineCharts

@Composable
fun ParametersDashboardRoot(
    viewModel: ElevatorParametersViewModel = viewModel(),
    navigationStateHolder: NavigationStateHolder
) {
    val state by viewModel.state.collectAsState()
    val hasParameters = state.parametersGroup.isNotEmpty()
    
    when {
        hasParameters -> ParametersDashboard(state = state)
        else -> DataConfigurationPrompt(
            title = stringResource(R.string.parameters_no_data_title),
            description = stringResource(R.string.parameters_no_data_description),
            actionButtonText = stringResource(R.string.parameters_no_data_button_text),
            launcher = { navigationStateHolder.setCurrentScreen(NavigationItem.ParametersConfigurations) },
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ParametersDashboard(
    state: ParametersState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = state.parametersGroup.lastOrNull()?.let { lastParam ->
                DateTimeUtils.formatFullDateTime(
                    timeSeconds = lastParam.timestamp,
                    timeMilliseconds = lastParam.timeMilliseconds
                )
            } ?: "",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        LineCharts(
            parameters = state.parametersGroup,
            chartConfig = state.chartConfig,
            onEvents = state.onEvents,
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}
//todo сделать Preview и отрефакторить