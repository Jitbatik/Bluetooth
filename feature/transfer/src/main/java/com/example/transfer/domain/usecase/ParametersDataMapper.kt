package com.example.transfer.domain.usecase

import com.example.transfer.domain.utils.ByteUtils.toIntFromByteData
import com.example.transfer.domain.utils.ByteUtils.toIntListFromByteData
import com.example.transfer.domain.utils.ByteUtils.toLongFromByteData
import com.example.transfer.model.ByteData
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.InputKey
import com.example.transfer.model.Parameter
import com.example.transfer.model.ParameterLabel
import com.example.transfer.model.ParameterPoint
import com.example.transfer.model.ParametersLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.max


class ParametersDataMapper @Inject constructor() {
    fun mergeParameters(
        existingParameters: List<Parameter>,
        byteDataList: List<ByteData>,
        chartParameters: MutableStateFlow<Map<Int, ChartParameters>>,
        autoScrollEnabled: MutableStateFlow<Boolean>,
    ): List<Parameter> {
        val timeStamp = byteDataList.subList(0, 4).toLongFromByteData().toInt()
        val parametersMap = existingParameters.associateBy { it.label }.toMutableMap()

        ParameterLabel.allValues().forEach { label ->
            val newValue = extractValueForLabel(label, byteDataList)

            val id = label.hashCode()
            val chartParameter = chartParameters.value[id]
            parametersMap[label] = parametersMap[label]
                ?.let {
                    updateExistingParameter(
                        existingTest = it,
                        timeStamp = timeStamp,
                        value = newValue,
                        chartParameter = chartParameter ?: ChartParameters()
                    )
                }
                ?: addNewParameter(
                    label = label,
                    timeStamp = timeStamp,
                    value = newValue,
                    chartParameter = chartParameter ?: ChartParameters()
                )

            val parameterPointsCount = parametersMap[label]?.points?.size ?: 0
            updateChartParameterForId(id, parameterPointsCount, chartParameters, autoScrollEnabled)
        }

        return parametersMap.values.map { parameter ->
            parameter.copy(points = mergeDuplicatePoints(parameter.points))
        }
    }

    private fun mergeDuplicatePoints(points: List<ParameterPoint>): List<ParameterPoint> {
        return points
            .groupBy { it.timeStamp }
            .map { (timeStamp, pointGroup) ->
                val avgValue = pointGroup.map { it.value }.average().toInt()
                ParameterPoint(timeStamp, avgValue)
            }
            .sortedBy { it.timeStamp }
    }

    private fun updateChartParameterForId(
        id: Int,
        parameterPointsCount: Int,
        chartParameters: MutableStateFlow<Map<Int, ChartParameters>>,
        autoScrollEnabled: MutableStateFlow<Boolean>
    ) {
        val shouldAutoScroll = autoScrollEnabled.value
        val currentParams = chartParameters.value.getOrElse(id) { ChartParameters() }
        val stepCount = interpolateSteps(
            minScale = currentParams.minScale,
            maxScale = currentParams.maxScale,
            currentScale = currentParams.scale
        )
        val newMaxOffset = max(0f, (parameterPointsCount - stepCount).toFloat())
        val newParams = currentParams.copy(
            minOffsetX = 0f,
            maxOffsetX = newMaxOffset,
            offset = if (shouldAutoScroll) newMaxOffset else currentParams.offset.coerceIn(
                0f,
                newMaxOffset
            )
        )
        autoScrollEnabled.value = newParams.offset >= newParams.maxOffsetX - 1f

        chartParameters.update { currentMap ->
            currentMap.toMutableMap().apply { this[id] = newParams }
        }

    }

    private fun addNewParameter(
        label: ParameterLabel,
        timeStamp: Int,
        value: Int,
        chartParameter: ChartParameters
    ): Parameter {
        return Parameter(
            id = label.hashCode(),
            label = label,
            stepCount = interpolateSteps(
                minScale = chartParameter.minScale,
                maxScale = chartParameter.maxScale,
                currentScale = chartParameter.scale,
//                minScalePoint ,
//                maxScalePoint ,
            ),
            points = listOf(ParameterPoint(timeStamp = timeStamp, value = value))
        )
    }

    private fun updateExistingParameter(
        existingTest: Parameter,
        timeStamp: Int,
        value: Int,
        chartParameter: ChartParameters
    ): Parameter {
        val newParameters =
            existingTest.points + ParameterPoint(timeStamp = timeStamp, value = value)
        return existingTest.copy(
            stepCount = interpolateSteps(
                minScale = chartParameter.minScale,
                maxScale = chartParameter.maxScale,
                currentScale = chartParameter.scale,
            ),
            points = newParameters
        )
    }

    private fun interpolateSteps(
        minScale: Float,
        maxScale: Float,
        minScalePoint: Int = 300,
        maxScalePoint: Int = 10,
        currentScale: Float,
    ): Int =
        (minScalePoint + ((currentScale - minScale) * (maxScalePoint - minScalePoint) / (maxScale - minScale))).toInt()

    private fun extractValueForLabel(label: ParameterLabel, byteDataList: List<ByteData>): Int {
        return when (label) {
            ParametersLabel.TIME_STAMP_MILLIS -> byteDataList.subList(4, 6).toIntFromByteData()
            ParametersLabel.FRAME_ID -> byteDataList.subList(6, 8).toIntFromByteData()
            in InputKey.entries -> {
                val index = InputKey.entries.indexOf(label)
                byteDataList.subList(8, 12).toIntListFromByteData()[index]
            }

            ParametersLabel.ENCODER_READINGS -> byteDataList.subList(12, 16).toIntFromByteData()
            ParametersLabel.ENCODER_FREQUENCY -> byteDataList.subList(16, 18).toIntFromByteData()
            ParametersLabel.ELEVATOR_SPEED -> byteDataList.subList(18, 20).toIntFromByteData()
            else -> throw IllegalArgumentException("Неизвестный параметр: $label")
        }
    }
}
