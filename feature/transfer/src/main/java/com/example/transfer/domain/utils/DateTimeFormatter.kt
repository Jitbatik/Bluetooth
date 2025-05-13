package com.example.transfer.domain.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    private const val FULL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
    private const val TIME_WITH_MILLIS_PATTERN = "HH:mm:ss.SSS"

    fun formatFullDateTime(timeSeconds: Long, timeMilliseconds: Int): String {
        val formatter = SimpleDateFormat(FULL_DATE_TIME_PATTERN, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val millis = timeSeconds * 1000 + timeMilliseconds
        return formatter.format(Date(millis))
    }

    fun formatTimeWithMillis(timeSeconds: Long, timeMilliseconds: Int): String {
        val formatter = SimpleDateFormat(TIME_WITH_MILLIS_PATTERN, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val millis = timeSeconds * 1000 + timeMilliseconds
        return formatter.format(Date(millis))
    }
} 