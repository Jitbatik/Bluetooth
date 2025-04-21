package com.example.transfer.domain.usecase

import com.example.transfer.domain.utils.ByteUtils.toIntFromByteData
import com.example.transfer.domain.utils.ByteUtils.toIntListFromByteData
import com.example.transfer.domain.utils.ByteUtils.toLongFromByteData
import com.example.transfer.model.ByteData
import com.example.transfer.model.ChartParameters
import com.example.transfer.model.DateTime
import com.example.transfer.model.LiftParameters
import com.example.transfer.model.Parameter
import com.example.transfer.model.ParametersGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject


class ProcessParametersFeatureCase @Inject constructor(
) {
    private val _currentChartParameters = MutableStateFlow(ChartParameters())
    val chartParameters: StateFlow<ChartParameters> = _currentChartParameters.asStateFlow()
    private val autoScrollEnabled = MutableStateFlow(true)

    fun updateChartParameter(newParams: ChartParameters) {
        _currentChartParameters.value = newParams
    }

    fun mapToParametersDataUI(dataFlow: Flow<List<ByteData>>): Flow<List<LiftParameters>> {
        return dataFlow.scan(emptyList(), ::mergeLiftParametersData)
    }

    private fun mergeLiftParametersData(
        existingParameters: List<LiftParameters>,
        byteDataList: List<ByteData>
    ) = existingParameters + LiftParameters(
        timeStamp = byteDataList.subList(0, 4).toLongFromByteData(),
        timeMilliseconds = byteDataList.subList(4, 6).toIntFromByteData(),
        frameId = byteDataList.subList(6, 8).toIntFromByteData(),
        data = byteDataList.subList(8, 12).toIntListFromByteData()
    )
}

class ProcessParametersFeatureCase1 @Inject constructor(
    private val parametersDataMapper: ParametersDataMapper,
) {
    private val _chartParameters = MutableStateFlow<Map<Int, ChartParameters>>(emptyMap())
    val chartParameters: StateFlow<Map<Int, ChartParameters>> = _chartParameters.asStateFlow()
    private val autoScrollEnabled = MutableStateFlow(true)

    fun updateChartParameter(chartId: Int, newParams: ChartParameters) {
        _chartParameters.update { currentMap ->
            currentMap + (chartId to newParams)
        }
    }

    fun mapToParametersDataUI(dataFlow: Flow<List<ByteData>>): Flow<ParametersGroup> {
        return dataFlow
            .scan(emptyList<Parameter>()) { existingParameters, byteDataList ->
                parametersDataMapper.mergeParameters(
                    existingParameters,
                    byteDataList,
                    _chartParameters,
                    autoScrollEnabled
                )
            }
            .filterNot { it.isEmpty() }
            .map(::updatedParametersGroup)
    }

    private fun updatedParametersGroup(listParameter: List<Parameter>): ParametersGroup {
        val time = listParameter.lastOrNull()?.points?.lastOrNull()?.timeStamp?.toLong() ?: 0
        return ParametersGroup(
            time = time.toDateTime(),
            data = listParameter
        )
    }
}


private fun Long.toDateTime(): DateTime {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@toDateTime * 1000 }
    return DateTime(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH),
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE),
        second = calendar.get(Calendar.SECOND)
    )
}