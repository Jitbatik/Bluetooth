package com.psis.transfer.chart.domain

import com.psis.transfer.xmlfileforchart.SignalSettingsParser
import javax.inject.Inject

class VersionSignalInfoProvider @Inject constructor(
    private val parser: SignalSettingsParser
) {
    private val cache by lazy { loadVersionField() }

    fun getVersionSignalInfo(): SignalSignalInfo = cache

    data class SignalSignalInfo(
        val offset: Int,
        val type: String
    )

    private fun loadVersionField(): SignalSignalInfo {
        val fields = parser.parse("l.xml")

        val versionField = fields.firstOrNull { it.name == "Ver" }
            ?: throw IllegalStateException("No 'Ver' field in l.xml")

        return SignalSignalInfo(
            offset = versionField.offset,
            type = versionField.type
        )
    }
}