package com.psis.transfer.chart.domain

import com.psis.transfer.chart.domain.model.SignalColor
import com.psis.transfer.chart.domain.model.SignalDefinition
import com.psis.transfer.chart.domain.model.SignalUserSettings
import kotlinx.coroutines.flow.Flow

interface SignalUserSettingsRepository {
    fun observe(): Flow<List<SignalUserSettings>>
    suspend fun updateVisibility(name: String, visible: Boolean)
    suspend fun updateColor(name: String, color: SignalColor)
    suspend fun initDefaults()
    suspend fun makeAllSignalsVisible()
}
