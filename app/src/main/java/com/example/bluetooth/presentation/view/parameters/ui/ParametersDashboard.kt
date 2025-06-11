package com.example.bluetooth.presentation.view.parameters.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluetooth.R
import com.example.bluetooth.presentation.navigation.NavigationStateHolder
import com.example.bluetooth.presentation.view.connect.components.enable.DataConfigurationPrompt
import com.example.bluetooth.presentation.view.parameters.viewmodel.ElevatorParametersViewModel
import com.example.bluetooth.presentation.view.parameters.viewmodel.ParametersEvents
import com.example.bluetooth.presentation.view.parameters.viewmodel.ParametersState
import override.navigation.NavigationItem

@Composable
fun ParametersDashboardRoot(
    viewModel: ElevatorParametersViewModel = hiltViewModel(),
    navigationStateHolder: NavigationStateHolder
) {
    val state by viewModel.state.collectAsState()
    val hasParameters = state.chartData.isNotEmpty()

    when {
        hasParameters -> ParametersDashboard(state = state, onEvents = viewModel.onEvents)
        else -> DataConfigurationPrompt(
            title = stringResource(R.string.parameters_no_data_title),
            description = stringResource(R.string.parameters_no_data_description),
            actionButtonText = stringResource(R.string.parameters_no_data_button_text),
            launcher = { navigationStateHolder.setCurrentScreen(NavigationItem.Settings) },
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ParametersDashboard(
    state: ParametersState,
    onEvents: (ParametersEvents) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = state.time,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LineCharts(
            chartData = state.chartData,
            parameterDisplayData = state.popData,
            chartConfig = state.chartConfig,
            onEvents = onEvents,
        )
    }
}
//todo сделать Preview и отрефакторить