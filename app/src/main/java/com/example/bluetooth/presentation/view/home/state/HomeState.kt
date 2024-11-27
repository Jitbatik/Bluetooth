package com.example.bluetooth.presentation.view.home.state

import androidx.compose.runtime.Stable
import com.example.bluetooth.presentation.view.home.CharUI
import com.example.bluetooth.presentation.view.home.HomeEvent
import com.example.domain.model.ControllerConfig

@Stable
data class HomeState(
    val data: List<CharUI>,
    val controllerConfig: ControllerConfig,
    val test: String,
    val onEvents: (HomeEvent) -> Unit,
)

enum class ButtonState {
    DEFAULT,
    PRESSED,
    ACTIVE,
}