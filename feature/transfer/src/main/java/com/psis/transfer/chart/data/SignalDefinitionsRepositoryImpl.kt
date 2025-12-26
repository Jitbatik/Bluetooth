package com.psis.transfer.chart.data

import com.psis.transfer.chart.domain.SignalDefinitionsRepository
import com.psis.transfer.chart.domain.model.SignalDefinition
import com.psis.transfer.xmlfileforchart.SignalSettingsParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SignalDefinitionsRepositoryImpl @Inject constructor(
    private val xmlReader: SignalSettingsParser
) : SignalDefinitionsRepository {
    private val lock = Mutex()

    private var cachedVersion: Int? = null
    private var cachedDefinitions: List<SignalDefinition> = emptyList()


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(versionFlow: Flow<Int>): Flow<List<SignalDefinition>> =
        versionFlow
            .distinctUntilChanged()
            .flatMapLatest { version -> flow { emit(loadIfNeeded(version)) } }
            .distinctUntilChanged()

    private suspend fun loadIfNeeded(version: Int): List<SignalDefinition> {
        lock.withLock {
            // если версия та же — отдаём кэш
            if (cachedVersion == version && cachedDefinitions.isNotEmpty()) {
                return cachedDefinitions
            }

            val fileName = resolveFileName(version)
            val parsed = xmlReader.parse(fileName)

            val mapped = parsed.map {
                SignalDefinition(
                    name = it.name,
                    comment = it.comment,
                    offset = it.offset,
                    type = it.type,
                    codes = it.codes
                )
            }

            cachedVersion = version
            cachedDefinitions = mapped
            return mapped
        }
    }

    private fun resolveFileName(version: Int): String = when (version) {
        0 -> "ver0.xml"
        1 -> "ver1.xml"
        2 -> "l.xml"
        else -> error("Unknown version: $version")
    }
}