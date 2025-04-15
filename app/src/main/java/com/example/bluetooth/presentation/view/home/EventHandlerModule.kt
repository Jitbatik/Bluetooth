package com.example.bluetooth.presentation.view.home

import ui.handler.EventHandlerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object EventHandlerModule {

    @Provides
    fun provideEventHandler(): EventHandler {
        return EventHandlerImpl()
    }
}