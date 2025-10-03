package com.psis.di

import com.psis.elimlift.data.ConnectRepositoryImpl
import com.psis.elimlift.data.DataStreamRepository
import com.psis.elimlift.data.ScannerRepositoryImpl
import com.psis.elimlift.domain.ConnectRepository
import com.psis.elimlift.domain.ScannerRepository
import com.psis.transfer.protocol.data.ProtocolPultDataRepository
import com.psis.transfer.protocol.data.ProtocolUARTDataRepository
import com.psis.transfer.protocol.domain.ProtocolDataRepository
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