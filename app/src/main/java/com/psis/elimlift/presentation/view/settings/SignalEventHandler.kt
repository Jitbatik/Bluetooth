package com.psis.elimlift.presentation.view.settings

import androidx.compose.ui.graphics.Color
import com.psis.elimlift.EventHandler
import com.psis.elimlift.presentation.view.settings.model.SignalEvent
import com.psis.transfer.chart.data.SignalUserSettingsRepositoryImpl
import com.psis.transfer.chart.domain.model.SignalColor
import javax.inject.Inject

class SignalEventHandler @Inject constructor(
    private val signalUserSettingsRepositoryImpl: SignalUserSettingsRepositoryImpl,
) : EventHandler<SignalEvent, Unit> {
    override suspend fun handle(event: SignalEvent) {
        when (event) {
            is SignalEvent.ToggleSignalVisibility -> signalUserSettingsRepositoryImpl
                .updateVisibility(event.signalName, event.isVisible)

            is SignalEvent.ChangeSignalColor -> signalUserSettingsRepositoryImpl
                .updateColor(
                    event.signalName,
                    event.color.toSignalColor()
                )

            is SignalEvent.MakeAllSignalsVisible -> signalUserSettingsRepositoryImpl.makeAllSignalsVisible()
        }
    }

    private fun Color.toSignalColor(): SignalColor =
        SignalColor((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
}