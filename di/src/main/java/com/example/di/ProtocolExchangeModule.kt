package com.example.di

import com.example.transfer.protocol.data.LiftExchangeProtocolImpl
import com.example.transfer.protocol.domain.ExchangeProtocol
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ProtocolExchangeModule {

    @Provides
    @Singleton
    fun provideExchangeProtocol(
        exchangeProtocolImpl: LiftExchangeProtocolImpl,
    ): ExchangeProtocol = exchangeProtocolImpl
}