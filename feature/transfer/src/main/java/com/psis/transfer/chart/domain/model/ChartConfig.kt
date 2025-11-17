package com.psis.transfer.chart.domain.model


data class ChartConfig(
    val scale: Float = 1f,
    val minScale: Float = 1f,
    val maxScale: Float = 2f,
    val offset: Float = 0f,
    val minOffsetX: Float = 0f,
    val maxOffsetX: Float = 0f,
    val minScalePoint: Int = 300,
    val maxScalePoint: Int = 5,

    // 🔥 новое поле
    val timeRange: TimeRange? = null
) {
    /**
     * Старый stepCount (если он тебе ещё нужен)
     */
    val stepCount: Int
        get() = (minScalePoint + ((scale - minScale) *
                (maxScalePoint - minScalePoint) / (maxScale - minScale))).toInt()

    /**
     * Новый step по диапазону времени
     */
    val timeStepCount: Int
        get() = timeRange?.durationSeconds?.toInt() ?: 0
}

data class TimeRange(
    val startMs: Long,
    val endMs: Long
) {
    /**
     * Длительность в секундах
     */
    val durationSeconds: Long
        get() = (endMs - startMs) / 1000

    fun isValid(): Boolean = endMs >= startMs

    fun updateStart(newStartMs: Long): TimeRange =
        copy(startMs = newStartMs)

    fun updateEnd(newEndMs: Long): TimeRange =
        copy(endMs = newEndMs)
}