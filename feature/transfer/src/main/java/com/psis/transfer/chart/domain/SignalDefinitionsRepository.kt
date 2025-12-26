package com.psis.transfer.chart.domain

import com.psis.transfer.chart.domain.model.SignalDefinition
import kotlinx.coroutines.flow.Flow

interface SignalDefinitionsRepository {
    // TODO ТЕХДОЛГ
    //  сделать метод который будет инитить чтобы не кидать в каждый обсерв versionFlow
    fun observe(versionFlow: Flow<Int>): Flow<List<SignalDefinition>>
}