package com.psis.transfer.protocol.data

import java.nio.charset.Charset

object LiftDataDefaults {
    private const val SENTENCE =
        "                    Соединение прерванноПроверьте Bluetooth                     "

    fun getDefault(): List<Byte> = SENTENCE
        .toByteArray(Charset.forName("windows-1251"))
        .toList()
}