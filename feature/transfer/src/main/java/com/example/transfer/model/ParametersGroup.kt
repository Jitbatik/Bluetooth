package com.example.transfer.model

enum class AxisOrientation { VERTICAL, HORIZONTAL }
data class TickLabel(val position: Float, val label: String)

data class LiftParameters(
    val timestamp: Long,
    val timeMilliseconds: Int,
    val frameId: Int,
    val parameters: List<ParameterData>,
)

data class ParameterData(
    val label: ParameterType,
    val value: Int
)

data class ChartConfig(
    val stepCount: Int = 20,
    val scale: Float = 1f,
    val minScale: Float = 1f,
    val maxScale: Float = 2f,
    val offset: Float = 0f,
    val minOffsetX: Float = 0f,
    val maxOffsetX: Float = 0f,
    val minScalePoint: Int = 300,
    val maxScalePoint: Int = 5,
)

sealed interface ParameterType { val displayName: String }

enum class LiftParameterType(
    override val displayName: String
) : ParameterType {
    TIMESTAMP_MILLIS("Timestamp (millis)"),
    FRAME_ID("Frame ID"),
    ENCODER_READINGS("Показания Энкодера"),
    ENCODER_FREQUENCY("Частота Энкодера"),
    ELEVATOR_SPEED("Скорость лифта"),
}