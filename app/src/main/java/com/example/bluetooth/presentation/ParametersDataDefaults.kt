package com.example.bluetooth.presentation

import com.example.transfer.model.LiftParameters
import com.example.transfer.model.ParametersLabel
import com.example.transfer.model.Test

object ParametersDataDefaults {
    fun getDefault() = listOf(
        LiftParameters(
            timeStamp = 1690854000L,
            timeMilliseconds = 800,
            frameId = 12,
            data = listOf( Test(ParametersLabel.TIME_STAMP_MILLIS, 0),)
        )
    )
}