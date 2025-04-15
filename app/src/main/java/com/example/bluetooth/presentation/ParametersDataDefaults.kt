package com.example.bluetooth.presentation

import com.example.transfer.model.ParameterPoint
import com.example.transfer.model.DateTime
import com.example.transfer.model.ParametersGroup
import com.example.transfer.model.ParametersLabel
import com.example.transfer.model.Parameter

object ParametersDataDefaults {
    fun getDefault() = ParametersGroup(
        time = DateTime(
            year = 1970,
            month = 1,
            day = 1,
            hour = 1,
            minute = 1,
            second = 1,
        ),
        data = listOf(
            Parameter(
                id = 999,
                stepCount = 50,
                label = ParametersLabel.TIME_STAMP_MILLIS,
                points =  listOf(
                    ParameterPoint(timeStamp = 1, value = 99),
                    ParameterPoint(timeStamp = 2, value = 90),
                    ParameterPoint(timeStamp = 4, value = 91),
                    ParameterPoint(timeStamp = 5, value = 91),
                    ParameterPoint(timeStamp = 6, value = 91),
                    ParameterPoint(timeStamp = 7, value = 91),
                    ParameterPoint(timeStamp = 8, value = 91)
                )
            )
        )
    )
}