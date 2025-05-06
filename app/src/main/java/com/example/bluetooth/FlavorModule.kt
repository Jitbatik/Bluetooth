package com.example.bluetooth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FlavorModule {

    @Provides
    @Singleton
    fun provideCurrentFlavor(): String {
        return BuildConfig.CURRENT_FLAVOR
    }
}