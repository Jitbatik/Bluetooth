package com.psis.transfer.chart.domain.model

data class DateTimeData(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val millisecond: Int,
)

val DateTimeData.dateString: String
    get() = "%02d.%02d.%04d".format(day, month, year)

val DateTimeData.timeString: String
    get() = "%02d:%02d:%02d.%03d".format(hour, minute, second, millisecond)