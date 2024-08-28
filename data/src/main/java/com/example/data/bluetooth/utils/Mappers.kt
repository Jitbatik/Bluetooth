package com.example.data.bluetooth.utils

import com.example.domain.model.CharData


fun List<ByteArray>.mapToListByte(): List<Byte> {
    return flatMap { byteArray ->
        byteArray.drop(6)
    }
}

fun List<Byte>.mapToListCharData(): List<CharData> {
    return map {
        CharData(charByte = it, colorByte = 1.toByte(), backgroundByte = 0.toByte())
    }
}

fun List<ByteArray>.mapToListCharDataFromArray(): List<CharData> {
    return mapToListByte().mapToListCharData()
}
