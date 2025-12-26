package com.psis.elimlift.presentation.view.parameters.ui

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psis.elimlift.presentation.navigation.NavigationStateHolder
import com.psis.elimlift.presentation.view.parameters.viewmodel.ElevatorParametersViewModel
import com.psis.elimlift.presentation.view.parameters.viewmodel.ParametersEvents
import com.psis.elimlift.presentation.view.parameters.viewmodel.ParametersState
import com.psis.transfer.chart.domain.model.DateTimeData
import com.psis.transfer.chart.domain.model.EditTarget
import com.psis.transfer.chart.domain.model.TimeRange
import java.util.Calendar
import kotlin.reflect.KClass

@Composable
fun ParametersDashboardRoot(
    viewModel: ElevatorParametersViewModel = hiltViewModel(),
    navigationStateHolder: NavigationStateHolder
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hasParameters by remember(state.chartData) {
        derivedStateOf { state.chartData.isNotEmpty() }
    }
    ParametersDashboard(state = state, onEvents = viewModel.onEvents)
//    when {
//        hasParameters -> ParametersDashboard(state = state, onEvents = viewModel.onEvents)
//        else -> DataConfigurationPrompt(
//            title = stringResource(R.string.parameters_no_data_title),
//            description = stringResource(R.string.parameters_no_data_description),
//            actionButtonText = stringResource(R.string.parameters_no_data_button_text),
//            launcher = { navigationStateHolder.setCurrentScreen(NavigationItem.Settings) },
//            modifier = Modifier.padding(8.dp)
//        )
//    }
}


@Composable
fun ParametersDashboard(
    state: ParametersState,
    onEvents: (ParametersEvents) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    // Какое исходное значение показывать в диалоге (start или end)
    var editingInitialValue by remember { mutableStateOf<DateTimeData?>(null) }
    // Date/Time picker type
    var pickerType by remember { mutableStateOf(PickerType.Date) }
    // Подтверждение выбора
    var onConfirm by remember { mutableStateOf<(DateTimeData) -> Unit>({}) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        TimeRangeBox(
            timeRange = state.timeRange,
            onEdit = { target, data ->
                editingInitialValue = data

                pickerType = when (target) {
                    EditTarget.StartDate, EditTarget.EndDate -> PickerType.Date
                    EditTarget.StartTime, EditTarget.EndTime -> PickerType.Time
                }

                onConfirm = { newValue ->
                    // Создаем новый TimeRange с обновленным значением
                    val newTimeRange = createUpdatedTimeRange(
                        oldRange = state.timeRange ?: getDefaultTimeRange(),
                        target = target,
                        newValue = newValue
                    )

                    onEvents(
                        ParametersEvents.EditTimeRange(
                            oldTimeRange = state.timeRange ?: getDefaultTimeRange(),
                            newTimeRange = newTimeRange
                        )
                    )
                }


                showDialog = true
            }
        )

        if (showDialog && editingInitialValue != null) {
            TimePickerDialog(
                type = pickerType,
                initial = editingInitialValue!!,
                onConfirm = onConfirm,
                onDismiss = { showDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LineCharts(
            chartData = state.chartData,
            parameterDisplayData = state.popData,
            touchPosition = state.tapPosition,
            chartConfig = state.chartConfig,
            onEvent = onEvents,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

// TODO убрать этот костыль
private fun getDefaultTimeRange(): TimeRange {
    // Дефолтный диапазон - последний час
    val now = System.currentTimeMillis()
    val fiveMinutesAgo = now - 300_000L

    return TimeRange(
        start = millisecondsToDateTimeData(fiveMinutesAgo),
        end = millisecondsToDateTimeData(now)
    )
}

private fun millisecondsToDateTimeData(timestamp: Long): DateTimeData {
    // Предполагаем, что у вас есть такая функция
    // Если нет, нужно реализовать конвертацию
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    return DateTimeData(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH),
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE),
        second = calendar.get(Calendar.SECOND),
        millisecond = calendar.get(Calendar.MILLISECOND)
    )
}

private fun createUpdatedTimeRange(
    oldRange: TimeRange,
    target: EditTarget,
    newValue: DateTimeData
): TimeRange {
    return when (target) {
        EditTarget.StartDate -> oldRange.copy(
            start = oldRange.start.copy(
                year = newValue.year,
                month = newValue.month,
                day = newValue.day
            )
        )
        EditTarget.StartTime -> oldRange.copy(
            start = oldRange.start.copy(
                hour = newValue.hour,
                minute = newValue.minute,
                second = newValue.second,
                millisecond = newValue.millisecond
            )
        )
        EditTarget.EndDate -> oldRange.copy(
            end = oldRange.end.copy(
                year = newValue.year,
                month = newValue.month,
                day = newValue.day
            )
        )
        EditTarget.EndTime -> oldRange.copy(
            end = oldRange.end.copy(
                hour = newValue.hour,
                minute = newValue.minute,
                second = newValue.second,
                millisecond = newValue.millisecond
            )
        )
    }
}

@Composable
private fun DateRangeSection(
    timeRange: TimeRange?,
    onEdit: (EditTarget, DateTimeData) -> Unit
) {
    if (timeRange == null) {
        Text(
            text = "--.--.--",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    if (timeRange.isSameDate) {
        Text(
            text = timeRange.dateStart,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onEdit(EditTarget.StartDate, timeRange.start)
                },
            textAlign = TextAlign.Center
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timeRange.dateStart,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                modifier = Modifier.clickable {
                    onEdit(EditTarget.StartDate, timeRange.start)
                }
            )
            Text(
                text = timeRange.dateEnd,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.clickable {
                    onEdit(EditTarget.EndDate, timeRange.end)
                }
            )
        }
    }
}

@Composable
private fun TimeRangeSection(
    timeRange: TimeRange?,
    onEdit: (EditTarget, DateTimeData) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (timeRange == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "--:--:--",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "--:--:--",
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }
            return
        }

        Text(
            text = timeRange.timeStart,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            modifier = Modifier.clickable {
                onEdit(EditTarget.StartTime, timeRange.start)
            }
        )
        Text(
            text = timeRange.timeEnd,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.clickable {
                onEdit(EditTarget.EndTime, timeRange.end)
            }
        )
    }
}


@Composable
fun TimeRangeBox(
    timeRange: TimeRange?,
    onEdit: (EditTarget, DateTimeData) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Даты
        DateRangeSection(timeRange = timeRange, onEdit = onEdit)

        Spacer(modifier = Modifier.height(8.dp))

        TimeRangeSection(timeRange = timeRange, onEdit = onEdit)
        // Время всегда в ряд

    }
}

enum class PickerType {
    Date,
    Time
}


@Composable
fun TimePickerDialog(
    initial: DateTimeData,
    type: PickerType,
    onDismiss: () -> Unit,
    onConfirm: (DateTimeData) -> Unit,
) {
    when (type) {
        PickerType.Date -> {
            DatePickerDialog(
                initial = initial,
                wheelPickerTypes = listOf(
                    WheelPickerType.Day,
                    WheelPickerType.Month,
                    WheelPickerType.Year(1970..2025)
                ),
                titleDialog = "Выберите дату",
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }

        PickerType.Time -> {
            DatePickerDialog(
                initial = initial,
                wheelPickerTypes = listOf(
                    WheelPickerType.Hour,
                    WheelPickerType.Minute,
                    WheelPickerType.Second,
                    WheelPickerType.Millisecond
                ),
                titleDialog = "Выберите время",
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
    }
}


sealed class WheelPickerType(
    val label: String,
    val defaultRange: IntRange
) {
    // ---- Дата ----
    data object Day : WheelPickerType("День", 1..31)
    data object Month : WheelPickerType("Месяц", 1..12)
    data class Year(val range: IntRange) : WheelPickerType("Год", range)

    // ---- Время ----
    data object Hour : WheelPickerType("Часы", 0..23)
    data object Minute : WheelPickerType("Мин", 0..59)
    data object Second : WheelPickerType("Сек", 0..59)
    data object Millisecond : WheelPickerType("Мс", 0..999)
}


private val getters: Map<KClass<out WheelPickerType>, DateTimeData.() -> Int> = mapOf(
    WheelPickerType.Year::class to { year },
    WheelPickerType.Month::class to { month },
    WheelPickerType.Day::class to { day },
    WheelPickerType.Hour::class to { hour },
    WheelPickerType.Minute::class to { minute },
    WheelPickerType.Second::class to { second },
    WheelPickerType.Millisecond::class to { millisecond }
)

private val setters: Map<KClass<out WheelPickerType>, DateTimeData.(Int) -> DateTimeData> = mapOf(
    WheelPickerType.Year::class to { copy(year = it) },
    WheelPickerType.Month::class to { copy(month = it) },
    WheelPickerType.Day::class to { copy(day = it) },
    WheelPickerType.Hour::class to { copy(hour = it) },
    WheelPickerType.Minute::class to { copy(minute = it) },
    WheelPickerType.Second::class to { copy(second = it) },
    WheelPickerType.Millisecond::class to { copy(millisecond = it) }
)

private fun DateTimeData.get(type: WheelPickerType): Int =
    getters[type::class]?.invoke(this) ?: error("No getter for $type")

private fun DateTimeData.set(type: WheelPickerType, value: Int): DateTimeData =
    setters[type::class]?.invoke(this, value) ?: error("No setter for $type")

fun daysInMonth(month: Int, year: Int): Int = when (month) {
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 31
}


fun DateTimeData.clampDay(): DateTimeData {
    val maxDay = daysInMonth(month, year)
    return if (day > maxDay) copy(day = maxDay) else this
}


@Composable
fun DatePickerDialog(
    initial: DateTimeData,
    wheelPickerTypes: List<WheelPickerType>,
    titleDialog: String = "",
    onConfirm: (DateTimeData) -> Unit,
    onDismiss: () -> Unit
) {
    var result by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = titleDialog) },
        text = {
            Column(
                modifier = Modifier
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    wheelPickerTypes.forEach { type ->
                        val range = remember(result.month, result.year, type) {
                            when (type) {
                                is WheelPickerType.Day -> 1..daysInMonth(result.month, result.year)
                                else -> type.defaultRange
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(type.label)
                            NumberPickerSample(
                                value = result.get(type),
                                range = range,
                                onValueChange = { new -> result = result.set(type, new).clampDay() }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(result)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun NumberPickerSample(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    key(range) {
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    this.value = value
                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            update = { picker ->
                // update всё равно полезен, если value изменится извне
                picker.minValue = range.first
                picker.maxValue = range.last
                if (picker.value != value) {
                    picker.value = value
                }
            }
        )
    }
}


//todo сделать Preview и отрефакторить

