package com.example.bluetooth.presentation.view.home

import androidx.compose.runtime.Stable
import com.example.transfer.protocol.domain.model.ControllerConfig

@Stable
data class HomeState(
    val data: List<DataUI>,
    val controllerConfig: ControllerConfig,
    val test: String,
    val onEvents: (HomeEvent) -> Unit,
    val isConnected: Boolean,
)