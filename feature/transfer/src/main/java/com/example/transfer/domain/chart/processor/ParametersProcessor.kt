package com.example.transfer.domain.chart.processor

import com.example.transfer.domain.utils.ByteUtils.toIntFromByteData
import com.example.transfer.domain.utils.ByteUtils.toLongFromByteData
import com.example.transfer.model.ByteData
import com.example.transfer.model.ChartConfig
import com.example.transfer.model.LiftParameterType
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParameterData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class ParametersProcessor @Inject constructor() {
    private var accumulatedParameters: List<LiftParameters> = emptyList()
    private val _filteredParameters = MutableStateFlow<List<LiftParameters>>(emptyList())

    fun getAccumulatedParameters(): List<LiftParameters> = accumulatedParameters

    fun processParameters(
        dataFlow: Flow<List<ByteData>>,
        chartConfig: StateFlow<ChartConfig>
    ): Flow<List<LiftParameters>> = dataFlow
        .scan(emptyList(), ::mergeLiftParametersData)
        .applyDynamicFilter(chartConfig)

    private fun mergeLiftParametersData(
        existingParameters: List<LiftParameters>,
        byteDataList: List<ByteData>
    ): List<LiftParameters> {
        val newParameters = createLiftParameters(byteDataList)
        val merged = if (existingParameters is ArrayList)
            existingParameters.also { it += newParameters }
        else
            existingParameters + newParameters

        return merged.takeLast(MAX_ACCUMULATED_PARAMETERS).also {
            accumulatedParameters = it
        }
    }

    private fun createLiftParameters(byteDataList: List<ByteData>) = LiftParameters(
        timestamp = byteDataList.subList(0, 4).toLongFromByteData(),
        timeMilliseconds = byteDataList.subList(4, 6).toIntFromByteData(),
        frameId = byteDataList.subList(6, 8).toIntFromByteData(),
        parameters = listOf(
            ParameterData(
                LiftParameterType.ENCODER_FREQUENCY,
                byteDataList.subList(16, 18).toIntFromByteData()
            ),
            ParameterData(
                LiftParameterType.ENCODER_READINGS,
                byteDataList.subList(6, 8).toIntFromByteData()
            )
        )
    )

    private fun Flow<List<LiftParameters>>.applyDynamicFilter(
        paramsFlow: StateFlow<ChartConfig>
    ): Flow<List<LiftParameters>> = combine(paramsFlow) { _, config ->
        filterByTimestampRange(
            parameters = accumulatedParameters,
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

        val baseTime = parameters.minOf { it.timestamp }

        val rangeStartSeconds = baseTime + offset.toLong()
        val rangeEndSeconds = rangeStartSeconds + count

        return parameters
            .asSequence()
            .filter { param ->
                param.timestamp in rangeStartSeconds..rangeEndSeconds
            }
            .sortedWith(
                compareBy(
                    LiftParameters::timestamp,
                    LiftParameters::timeMilliseconds
                )
            )
            .toList()
    }

    fun refreshFilter(config: ChartConfig) {
        _filteredParameters.value = filterByTimestampRange(
            parameters = accumulatedParameters,
            offset = config.offset,
            count = config.stepCount
        )
    }

    companion object {
        private const val MAX_ACCUMULATED_PARAMETERS = 1000
    }
}