package com.example.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.bluetooth.provider.BluetoothSocketProvider
import com.example.domain.repository.ExchangeDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EXCHANGE_DATA_REPOSITORY_IMPL = "MANAGE_DATA_REPOSITORY_IMPL"

class ExchangeDataRepositoryImpl @Inject constructor(
    private val bluetoothSocketProvider: BluetoothSocketProvider,
) : ExchangeDataRepository {
    private var socket: BluetoothSocket? = null

    override val isSocket: Flow<Boolean>
        get() = bluetoothSocketProvider.bluetoothSocket
            .map { it != null }


//    private val _inputStream: InputStream
//        get() = socket.inputStream
//
//    private val _outputStream: OutputStream
//        get() = socket.outputStream
//
//
//    //
//
//    override fun readFromStream(canRead: Boolean): Flow<ByteArray> {
//        val buffer = ByteArray(4 * 1_024)
//        return flow {
//            while (canRead && socket.isConnected && currentCoroutineContext().isActive) {
//                try {
//                    buffer.fill(0x0)
//                    val bytesRead = withContext(Dispatchers.IO) {
//                        _inputStream.read(buffer)
//                    }
//                    if (bytesRead > 0) {
//                        emit(buffer.copyOf(bytesRead))
//                    }
//                } catch (e: IOException) {
//                    Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error reading stream", e)
//                    break
//                }
//            }
//        }
//    }
//
//    override suspend fun sendToStream(value: String): Result<Boolean> {
//        return withContext(Dispatchers.IO) {
//            try {
//                if (!socket.isConnected)
//                    return@withContext Result.failure(SecurityException("No connected socket"))
//                _outputStream.write(value.toByteArray())
//                Log.d(EXCHANGE_DATA_REPOSITORY_IMPL, "WRITTEN TO STREAM")
//                Result.success(true)
//            } catch (e: IOException) {
//                Log.e(EXCHANGE_DATA_REPOSITORY_IMPL, "Error sending data to stream", e)
//                Result.failure(e)
//            }
//        }
//
//    }
}