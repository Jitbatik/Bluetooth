package com.psis.elimlift.presentation.rssi

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val STRONG_SIGNAL_COLOR = Color.Green
private val MEDIUM_SIGNAL_COLOR = Color(0xFFFFC107) // Amber
private val WEAK_SIGNAL_COLOR = Color.Red
private val NO_SIGNAL_COLOR = Color.Gray

fun Flow<Int>.toUIRssi(): Flow<RSSI> = map { value ->
    val color = when {
        value > -25 -> STRONG_SIGNAL_COLOR
        value > -35 -> MEDIUM_SIGNAL_COLOR
        value > -45 -> WEAK_SIGNAL_COLOR
        else -> NO_SIGNAL_COLOR
    }
    RSSI(rssi = "$value", color = color)
}