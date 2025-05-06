package com.example.di

import com.example.bluetooth.data.ConnectRepositoryImpl
import com.example.bluetooth.data.DataStreamRepository
import com.example.bluetooth.data.ScannerRepositoryImpl
import com.example.bluetooth.domain.ConnectRepository
import com.example.bluetooth.domain.ScannerRepository
import com.example.transfer.data.ProtocolLiftDataRepository
import com.example.transfer.data.ProtocolPultDataRepository
import com.example.transfer.data.ProtocolUARTDataRepository
import com.example.transfer.domain.ProtocolDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProtocolDataRepository(
        flavor: String,
        dataStreamRepository: DataStreamRepository
    ): ProtocolDataRepository {
        return when (flavor) {
            "pult" -> ProtocolPultDataRepository(dataStreamRepository)
            "lift" -> ProtocolLiftDataRepository(dataStreamRepository)
            "uart" -> ProtocolUARTDataRepository(dataStreamRepository)
            else -> throw IllegalArgumentException("Unknown flavor: $flavor")
        }
    }

    @Provides
    @Singleton
    fun provideScannerRepository(
        scannerRepositoryImpl: ScannerRepositoryImpl,
    ): ScannerRepository = scannerRepositoryImpl

    @Provides
    @Singleton
    fun provideConnectRepository(
        connectRepositoryImpl: ConnectRepositoryImpl,
    ): ConnectRepository = connectRepositoryImpl
}