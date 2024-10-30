package com.example.data.bluetooth.utils

import com.example.domain.model.CharData


fun List<ByteArray>.mapToListByte(): List<Byte> {
    return flatMap { byteArray ->
        byteArray.drop(6)
    }
}

fun List<Byte>.mapToListCharData(): List<CharData> {
    return map {
        CharData(charByte = it, colorByte = 0, backgroundByte = 15)
    }
}

fun List<ByteArray>.mapToListCharDataFromArray(): List<CharData> {
    return mapToListByte().mapToListCharData()
}
