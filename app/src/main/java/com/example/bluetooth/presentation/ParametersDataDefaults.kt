package com.example.bluetooth.presentation

import com.example.transfer.model.LiftParameters

object ParametersDataDefaults {
    fun getDefault() = listOf(
        LiftParameters(
            timeStamp = 1690854000L,
            timeMilliseconds = 800,
            frameId = 12,
            data = listOf(0, 1, 1, 1, 19)
        )
    )
}