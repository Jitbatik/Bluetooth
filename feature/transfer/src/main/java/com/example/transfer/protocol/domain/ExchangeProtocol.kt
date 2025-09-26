package com.example.transfer.protocol.domain

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.flow.Flow

interface ExchangeProtocol {
    fun request(socket: BluetoothSocket, bytes: ByteArray)
    fun sendCommand(socket: BluetoothSocket, command: ByteArray)
    fun listen(socket: BluetoothSocket): Flow<List<Byte>>
}