package com.psis.elimlift.presentation.view.parameters.mapper

import com.psis.transfer.chart.domain.model.DisplayValueWithColor as DomainDisplayValue
import com.psis.elimlift.presentation.view.parameters.model.DisplayValueWithColor
import com.psis.elimlift.presentation.view.parameters.util.toUiColor

fun DomainDisplayValue.toUi(): DisplayValueWithColor =
    DisplayValueWithColor(
        value = this.value,
        color = this.color.toUiColor()
    )