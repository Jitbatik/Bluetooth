package com.example.bluetooth.presentation.view.settings

import androidx.compose.ui.graphics.Color
import com.example.transfer.chart.data.ChartSettingsRepository
import com.example.bluetooth.EventHandler
import com.example.transfer.chart.domain.model.SignalColor
import com.example.bluetooth.presentation.view.settings.model.SignalEvent
import javax.inject.Inject

class SignalEventHandler @Inject constructor(
    private val chartSettingsRepository: ChartSettingsRepository
) : EventHandler<SignalEvent, Unit> {
    override fun handle(event: SignalEvent) {
        when (event) {
            is SignalEvent.ToggleSignalVisibility -> chartSettingsRepository.toggleSignalVisibility(
                event.signalId, event.isVisible
            )

            is SignalEvent.ChangeSignalColor -> chartSettingsRepository.changeSignalColor(
                event.signalId, event.color.toSignalColor()
            )

            is SignalEvent.MakeAllSignalsVisible -> chartSettingsRepository.makeAllSignalsVisible()
        }
    }

    private fun Color.toSignalColor(): SignalColor =
        SignalColor((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
}