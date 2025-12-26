package com.psis.transfer.chart.domain

import com.psis.transfer.chart.domain.model.DateTimeData
import java.util.Calendar
import java.util.TimeZone

fun DateTimeData.toTimestampMs(): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1) // Calendar: 0-based months
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, second)
        set(Calendar.MILLISECOND, millisecond)
    }

    return calendar.timeInMillis
}

fun millisecondsToDateTimeData(timestamp: Long): DateTimeData {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = timestamp
    }
    return DateTimeData(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH),
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE),
        second = calendar.get(Calendar.SECOND),
        millisecond = calendar.get(Calendar.MILLISECOND)
    )
}