package com.example.transfer.domain.usecase

import com.example.transfer.domain.utils.ByteUtils.toIntFromByteData
import com.example.transfer.domain.utils.ByteUtils.toLongFromByteData
import com.example.transfer.model.ByteData
import com.example.transfer.model.ChartConfig
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.LiftParameterType
import com.example.transfer.model.ParameterData
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
    private val _chartConfig = MutableStateFlow(ChartConfig())
    val chartConfig: StateFlow<ChartConfig> = _chartConfig.asStateFlow()

    private val _autoScrollEnabled = MutableStateFlow(true)

    private var accumulatedParameters: List<LiftParameters> = emptyList()

    fun updateChartParameter(newParams: ChartConfig) {
        val updatedParams = if (newParams.scale != _chartConfig.value.scale)
            newParams.copy(stepCount = calculateStepCount(newParams))
        else newParams

        _chartConfig.update { _ ->
            val updated = updatedParams.withUpdatedMaxOffset(accumulatedParameters)
            _autoScrollEnabled.value = updated.offset >= updated.maxOffsetX - 1f
            updated
        }
    }

    private fun calculateStepCount(
        param: ChartConfig,
    ): Int =
        (param.minScalePoint + ((param.scale - param.minScale) *
                (param.maxScalePoint - param.minScalePoint) / (param.maxScale - param.minScale))).toInt()

    private fun ChartConfig.withUpdatedMaxOffset(
        parameters: List<LiftParameters>
    ): ChartConfig {
        if (parameters.isEmpty()) return this.copy(maxOffsetX = 0f)

        val minTime = parameters.minOf { it.timestamp }
        val maxTime = parameters.maxOf { it.timestamp }
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
        .applyDynamicFilter(_chartConfig)


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
        timestamp = byteDataList.subList(0, 4).toLongFromByteData(),
        timeMilliseconds = byteDataList.subList(4, 6).toIntFromByteData(),
        frameId = byteDataList.subList(6, 8).toIntFromByteData(),
        // todo нужно сделать потом
        parameters = listOf(
            ParameterData(
                LiftParameterType.ENCODER_FREQUENCY,
                byteDataList.subList(16, 18).toIntFromByteData()
            )
        )
    )

    private fun Flow<List<LiftParameters>>.applyDynamicFilter(
        paramsFlow: StateFlow<ChartConfig>
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
            param.timestamp + param.timeMilliseconds / 1000f
        }

        val rangeStart = parameters.minOf { it.timestamp } + offset
        val rangeEnd =
            (rangeStart + count).coerceAtMost(parameters.maxOf { it.timestamp }.toFloat())

        return parameters
            .asSequence()
            .filter {
                val time = timeResolver(it)
                time in rangeStart..rangeEnd
            }
            .sortedWith(
                compareBy(
                    LiftParameters::timestamp,
                    LiftParameters::timeMilliseconds
                )
            )
            .toList()
    }
}