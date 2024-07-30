package com.example.di

import com.example.data.BluetoothRepositoryImpl
import com.example.data.ConnectRepositoryImpl
import com.example.data.PermissionRepositoryImpl
import com.example.domain.repository.BluetoothRepository
import com.example.domain.repository.ConnectRepository
import com.example.domain.repository.PermissionRepository
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
        bluetoothRepository: BluetoothRepositoryImpl
    ) : BluetoothRepository

    @Binds
    @Singleton
    abstract fun bindConnectRepository(
        connectRepository: ConnectRepositoryImpl
    ) : ConnectRepository


    /**
     * ХЗ правильно ли */
    @Binds
    @Singleton
    abstract fun bindPermissionRepository(
        permissionRepository: BluetoothRepositoryImpl
    ) : PermissionRepository
}