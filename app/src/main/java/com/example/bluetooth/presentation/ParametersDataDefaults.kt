package com.example.bluetooth.presentation

import com.example.transfer.model.LiftParameters
import com.example.transfer.model.LiftParameterType
import com.example.transfer.model.ParameterData

object ParametersDataDefaults {
    fun getDefault() = listOf(
        LiftParameters(
            timestamp = 1690854000L,
            timeMilliseconds = 800,
            frameId = 12,
            parameters = listOf(ParameterData(LiftParameterType.TIMESTAMP_MILLIS, 0))
        )
    )
}