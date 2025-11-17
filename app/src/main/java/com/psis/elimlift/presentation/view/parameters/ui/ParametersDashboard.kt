package com.psis.elimlift.presentation.view.parameters.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psis.elimlift.R
import com.psis.elimlift.presentation.navigation.NavigationStateHolder
import com.psis.elimlift.presentation.view.connect.components.enable.DataConfigurationPrompt
import com.psis.elimlift.presentation.view.parameters.viewmodel.ElevatorParametersViewModel
import com.psis.elimlift.presentation.view.parameters.viewmodel.ParametersEvents
import com.psis.elimlift.presentation.view.parameters.viewmodel.ParametersState
import override.navigation.NavigationItem

@Composable
fun ParametersDashboardRoot(
    viewModel: ElevatorParametersViewModel = hiltViewModel(),
    navigationStateHolder: NavigationStateHolder
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hasParameters by remember(state.chartData) {
        derivedStateOf { state.chartData.isNotEmpty() }
    }

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
    var timeRangeStart = remember { SimpleTime(2025, 10, 20, 13, 15, 15) }
    var timeRangeEnd = remember { SimpleTime(2025, 10, 20, 13, 20, 15) }

    val dataStart = Data(timeRangeStart.year, timeRangeStart.month, timeRangeStart.dayMonth)
    val timeStart = Time(timeRangeStart.hour, timeRangeStart.minute, timeRangeStart.second)
    val dataEnd = Data(timeRangeEnd.year, timeRangeEnd.month, timeRangeEnd.dayMonth)
    val timeEnd = Time(timeRangeEnd.hour, timeRangeEnd.minute, timeRangeEnd.second)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
//        TimeRangeBox(
//            dataStart = dataStart,
//            timeStart = timeStart,
//            dataEnd = dataEnd,
//            timeEnd = timeEnd,
////            showPicker = {
////                // здесь открываем диалог выбора времени/даты
////            }
//        )
        Text(
            text = state.time,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        LineCharts(
            chartData = state.chartData,
            parameterDisplayData = state.popData,
            touchPosition = state.tapPosition,
            chartConfig = state.chartConfig,
            onEvent = onEvents,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
        )
    }
}


data class SimpleTime(
    val year: Int,
    val month: Int,
    val dayMonth: Int,
    val hour: Int,
    val minute: Int,
    val second: Int
) {
    override fun toString(): String =
        "%04d-%02d-%02d %02d:%02d:%02d".format(year, month, dayMonth, hour, minute, second)
}

data class Data(
    val year: Int,
    val month: Int,
    val dayMonth: Int,
)
data class Time(
    val hour: Int,
    val minute: Int,
    val second: Int
)

@Composable
fun TimeRangeBox(
    dataStart: Data,
    timeStart: Time,
    dataEnd: Data,
    timeEnd: Time,
    onTimeSelected: (SimpleTime) -> Unit = {}
) {
    var showPicker by remember { mutableStateOf(false) }

    val items = listOf(
        "%04d-%02d-%02d".format(dataStart.year, dataStart.month, dataStart.dayMonth),
        "%02d:%02d:%02d".format(timeStart.hour, timeStart.minute, timeStart.second),
        "%04d-%02d-%02d".format(dataEnd.year, dataEnd.month, dataEnd.dayMonth),
        "%02d:%02d:%02d".format(timeEnd.hour, timeEnd.minute, timeEnd.second)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
//            .clickable { showPicker() }
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items.size) { index ->
            Text(
                text = items[index],
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
//                    .fillMaxWidth()
            )
        }
    }
}


@Composable
fun SimpleTimePickerDialog(
    initial: SimpleTime,
    onConfirm: (SimpleTime) -> Unit,
    onDismiss: () -> Unit
) {
    var editMode by remember { mutableStateOf(false) }   // false → барабаны, true → текстовый ввод
    var tempTime by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Выбор времени")

                IconButton(onClick = { editMode = !editMode }) {
                    Icon(
                        imageVector = if (editMode) Icons.Default.AccessTime else Icons.Default.Keyboard,
                        contentDescription = null
                    )
                }
            }
        },
        text = {
            if (editMode) {
                ManualTimeEditor(tempTime) { tempTime = it }
            } else {
                RollerTimePicker(tempTime) { tempTime = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempTime) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun ManualTimeEditor(
    time: SimpleTime,
    onChange: (SimpleTime) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = time.year.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { onChange(time.copy(year = it)) } },
                label = { Text("Год") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = time.month.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { onChange(time.copy(month = it)) } },
                label = { Text("Мес") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = time.dayMonth.toString(),
                onValueChange = { v ->
                    v.toIntOrNull()?.let { onChange(time.copy(dayMonth = it)) }
                },
                label = { Text("День") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = time.hour.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { onChange(time.copy(hour = it)) } },
                label = { Text("Часы") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = time.minute.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { onChange(time.copy(minute = it)) } },
                label = { Text("Мин") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = time.second.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { onChange(time.copy(second = it)) } },
                label = { Text("Сек") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RollerTimePicker(
    time: SimpleTime,
    onChange: (SimpleTime) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        RollerNumberPicker(
            values = (0..23).toList(),
            selected = time.hour,
            label = "Часы",
            onSelected = { onChange(time.copy(hour = it)) }
        )

        RollerNumberPicker(
            values = (0..59).toList(),
            selected = time.minute,
            label = "Мин",
            onSelected = { onChange(time.copy(minute = it)) }
        )

        RollerNumberPicker(
            values = (0..59).toList(),
            selected = time.second,
            label = "Сек",
            onSelected = { onChange(time.copy(second = it)) }
        )
    }
}


@Composable
fun RollerNumberPicker(
    values: List<Int>,
    selected: Int,
    label: String,
    onSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = values.indexOf(selected)
    )

    LaunchedEffect(selected) {
        listState.animateScrollToItem(values.indexOf(selected))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)

        Box(
            modifier = Modifier
                .height(120.dp)
                .width(70.dp)
        ) {

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(values) { item ->
                    Text(
                        text = item.toString().padStart(2, '0'),
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .clickable { onSelected(item) },
                        style = if (item == selected)
                            MaterialTheme.typography.titleMedium
                        else
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


//todo сделать Preview и отрефакторить

