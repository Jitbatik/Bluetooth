package com.example.bluetooth.presentation.view.parameters.mapper

import com.example.transfer.chart.domain.model.DisplayValueWithColor as DomainDisplayValue
import com.example.bluetooth.presentation.view.parameters.model.DisplayValueWithColor
import com.example.bluetooth.presentation.view.parameters.util.toUiColor

fun DomainDisplayValue.toUi(): DisplayValueWithColor =
    DisplayValueWithColor(
        value = this.value,
        color = this.color.toUiColor()
    )