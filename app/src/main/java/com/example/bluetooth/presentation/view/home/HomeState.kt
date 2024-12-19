package com.example.bluetooth.presentation.view.home

import androidx.compose.runtime.Stable
import com.example.transfer.model.ControllerConfig

@Stable
data class HomeState(
    val data: List<CharUI>,
    val controllerConfig: ControllerConfig,
    val test: String,
    val onEvents: (HomeEvent) -> Unit,
)