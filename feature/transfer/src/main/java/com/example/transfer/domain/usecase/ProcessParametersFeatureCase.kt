package com.example.transfer.domain.usecase

import com.example.transfer.domain.utils.ByteUtils.toIntFromByteData
import com.example.transfer.domain.utils.ByteUtils.toLongFromByteData
import com.example.transfer.model.ByteData
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParametersLabel
import com.example.transfer.model.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class ProcessParametersFeatureCase @Inject constructor(
) {
    private val _chartParameters = MutableStateFlow(ChartParameters())
    val chartParameters: StateFlow<ChartParameters> = _chartParameters.asStateFlow()

    private val _autoScrollEnabled = MutableStateFlow(true)

    private var accumulatedParameters: List<LiftParameters> = emptyList()

    fun updateChartParameter(newParams: ChartParameters) {
        val updatedParams = if (newParams.scale != _chartParameters.value.scale)
            newParams.copy(stepCount = calculateStepCount(newParams))
        else newParams

        _chartParameters.update { _ ->
            val updated = updatedParams.withUpdatedMaxOffset(accumulatedParameters)
            _autoScrollEnabled.value = updated.offset >= updated.maxOffsetX - 1f
            updated
        }
    }

    private fun calculateStepCount(
        param: ChartParameters,
    ): Int =
        (param.minScalePoint + ((param.scale - param.minScale) *
                (param.maxScalePoint - param.minScalePoint) / (param.maxScale - param.minScale))).toInt()

    private fun ChartParameters.withUpdatedMaxOffset(
        parameters: List<LiftParameters>
    ): ChartParameters {
        if (parameters.isEmpty()) return this.copy(maxOffsetX = 0f)

        val minTime = parameters.minOf { it.timeStamp }
        val maxTime = parameters.maxOf { it.timeStamp }
        val calculatedMaxOffset = (maxTime - minTime - stepCount).toFloat().coerceAtLeast(0f)

        val newOffset = when {
            _autoScrollEnabled.value -> calculatedMaxOffset
            else -> offset.coerceIn(0f, calculatedMaxOffset)
        }

        return this.copy(
            maxOffsetX = maxOffsetX,
            offset = newOffset
        )
    }

    fun mapToParametersDataUI(dataFlow: Flow<List<ByteData>>) = dataFlow
        .scan(emptyList(), ::mergeLiftParametersData)
        .onEach { accumulatedParameters = it }
        .applyDynamicFilter(_chartParameters)


    private fun mergeLiftParametersData(
        existingParameters: List<LiftParameters>,
        byteDataList: List<ByteData>
    ): List<LiftParameters> {
        return if (existingParameters is ArrayList)
            existingParameters.also { it += createLiftParameters(byteDataList) }
        else
            existingParameters + createLiftParameters(byteDataList)

    }

    private fun createLiftParameters(byteDataList: List<ByteData>) = LiftParameters(
        timeStamp = byteDataList.subList(0, 4).toLongFromByteData(),
        timeMilliseconds = byteDataList.subList(4, 6).toIntFromByteData(),
        frameId = byteDataList.subList(6, 8).toIntFromByteData(),
        // todo нужно сделать потом
        data = listOf(
            Test(
                ParametersLabel.ENCODER_FREQUENCY,
                byteDataList.subList(16, 18).toIntFromByteData()
            )
        )
    )

    private fun Flow<List<LiftParameters>>.applyDynamicFilter(
        paramsFlow: StateFlow<ChartParameters>
    ): Flow<List<LiftParameters>> = combine(paramsFlow) { data, config ->
        filterByTimestampRange(
            parameters = data,
            offset = config.offset,
            count = config.stepCount
        )
    }

    private fun filterByTimestampRange(
        parameters: List<LiftParameters>,
        offset: Float,
        count: Int
    ): List<LiftParameters> {
        if (parameters.isEmpty()) return emptyList()
        val timeResolver = { param: LiftParameters ->
            param.timeStamp + param.timeMilliseconds / 1000f
        }

        val rangeStart = parameters.minOf { it.timeStamp } + offset
        val rangeEnd =
            (rangeStart + count).coerceAtMost(parameters.maxOf { it.timeStamp }.toFloat())

        return parameters
            .asSequence()
            .filter {
                val time = timeResolver(it)
                time in rangeStart..rangeEnd
            }
            .sortedWith(
                compareBy(
                    LiftParameters::timeStamp,
                    LiftParameters::timeMilliseconds
                )
            )
            .toList()
    }
}