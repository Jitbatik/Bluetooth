package com.psis.transfer.chart.domain.usecase

import com.psis.transfer.chart.domain.model.DisplayValueWithColor
import com.psis.transfer.chart.domain.model.Line
import com.psis.transfer.chart.domain.model.ParameterDisplayData
import com.psis.transfer.chart.domain.model.SignalColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObservePopDataUseCase @Inject constructor() {

    // TODO ТЕХДОЛГ: иногда идеи приходят поздно
    //  в репозитории реализовать поиск по времени определенного фрейма условно getFrame(time: Long)
    operator fun invoke(
        linesFlow: Flow<List<Line>>,
        selectedIndexFlow: StateFlow<Int?>
    ): Flow<ParameterDisplayData> =
        combine(linesFlow, selectedIndexFlow) { lines, selectedIndex ->
            if (selectedIndex == null || lines.isEmpty()) {
                return@combine ParameterDisplayData()
            }

            // Проверяем первую линию
            val firstLine = lines.first()
            if (selectedIndex < 0 || selectedIndex >= firstLine.points.size) {
                return@combine ParameterDisplayData()
            }

            val params = lines.associate { line ->
                val point = line.points.getOrNull(selectedIndex)
                line.name to DisplayValueWithColor(
                    value = point?.value?.toFloat() ?: 0f,
                    color = SignalColor(55, 66, 77)
                )
            }

            val selectedPoint = firstLine.points[selectedIndex]
            ParameterDisplayData(
                timestamp = selectedPoint.time,
                timeMilliseconds = selectedPoint.millis,
                parameters = params
            )
        }
}
