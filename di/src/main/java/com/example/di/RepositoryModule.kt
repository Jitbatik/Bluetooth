package com.example.di

import com.example.data.ConnectRepositoryImpl
import com.example.data.ScannerRepositoryImpl
import com.example.domain.repository.ConnectRepository
import com.example.domain.repository.ScannerRepository
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
    abstract fun bindBluetoothRepository(
        scannerRepository: ScannerRepositoryImpl
    ) : ScannerRepository

    @Binds
    @Singleton
    abstract fun bindConnectRepository(
        connectRepository: ConnectRepositoryImpl
    ) : ConnectRepository
}