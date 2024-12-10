package com.example.di

import com.example.bluetooth.data.ConnectRepositoryImpl
import com.example.bluetooth.data.ScannerRepositoryImpl
import com.example.bluetooth.domain.ConnectRepository
import com.example.bluetooth.domain.ScannerRepository
import com.example.data.ExchangeDataRepositoryImpl
import com.example.domain.repository.ExchangeDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScannerRepository(
        scannerRepository: ScannerRepositoryImpl,
    ): ScannerRepository

    @Binds
    @Singleton
    abstract fun bindConnectRepository(
        connectRepository: ConnectRepositoryImpl,
    ): ConnectRepository

    @Binds
    @Singleton
    abstract fun bindExchangeDataRepository(
        exchangeDataRepository: ExchangeDataRepositoryImpl,
    ): ExchangeDataRepository

}