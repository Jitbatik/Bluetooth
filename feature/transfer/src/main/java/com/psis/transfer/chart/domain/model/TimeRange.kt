package com.psis.transfer.chart.domain.model

data class TimeRange(
    val start: DateTimeData,
    val end: DateTimeData,
) {
    val dateStart get() = start.dateString
    val dateEnd get() = end.dateString

    val timeStart get() = start.timeString
    val timeEnd get() = end.timeString


    val isSameDate: Boolean
        get() = start.year == end.year && start.month == end.month && start.day == end.day
}