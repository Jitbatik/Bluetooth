package com.psis.elimlift.presentation.view.home

import com.psis.elimlift.EventHandler
import override.ui.EventHandlerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object EventHandlerModule {

    @Provides
    fun provideEventHandler(): EventHandler<HomeEvent, ByteArray> {
        return EventHandlerImpl()
    }
}