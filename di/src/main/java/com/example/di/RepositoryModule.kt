package com.example.di

import com.example.data.BluetoothDeviseRepositoryImpl
import com.example.data.ConnectRepositoryImpl
import com.example.domain.repository.BluetoothDeviseRepository
import com.example.domain.repository.ConnectRepository
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
    abstract fun bindBluetoothDeviseRepository(
        bluetoothDeviseRepository: BluetoothDeviseRepositoryImpl
    ) : BluetoothDeviseRepository

    @Binds
    @Singleton
    abstract fun bindConnectRepository(
        connectRepository: ConnectRepositoryImpl
    ) : ConnectRepository
}