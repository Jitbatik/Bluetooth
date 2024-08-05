package com.example.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.data.receivers.RemoteDeviceUUIDReceiver
import com.example.domain.model.BluetoothDevice
import com.example.domain.repository.ConnectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

const val ConnectRepositoryImpl_LOGGER = "ConnectRepositoryImpl_LOGGER"

@SuppressLint("MissingPermission")
class ConnectRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
): ConnectRepository {

    private val _bluetoothManager by lazy { context.getSystemService<BluetoothManager>() }
    private val _bluetoothAdapter: BluetoothAdapter?
        get() = _bluetoothManager?.adapter


    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private fun fetchUUIDs(address: String): Flow<List<UUID>> = callbackFlow {
        val device = _bluetoothAdapter?.getRemoteDevice(address)

        val remoteDeviceUUIDReceiver = RemoteDeviceUUIDReceiver(
            onReceivedUUIDs = { uuids ->
                // remove the client-server uuid
                val uuidSet = uuids.distinct()
                Log.d(ConnectRepositoryImpl_LOGGER, "FOUND UUIDS: $uuids")
                scope.launch { send(uuidSet) }
            },
        )

        ContextCompat.registerReceiver(
            context,
            remoteDeviceUUIDReceiver,
            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_UUID),
            ContextCompat.RECEIVER_EXPORTED
        )
        // fetch uuids using service discovery protocol
        val isOk = device?.fetchUuidsWithSdp()
        Log.d(ConnectRepositoryImpl_LOGGER, "DEVICE UUID FETCH STATUS $isOk")

        awaitClose {
            context.unregisterReceiver(remoteDeviceUUIDReceiver)
            scope.cancel()
            Log.d(ConnectRepositoryImpl_LOGGER, "RECEIVER_FOR_UUID_REMOVED")
        }
    }

    override fun connectToDevice(bluetoothDevice: BluetoothDevice): Boolean {
        return true
    }

    override fun disconnectFromDevice(): Boolean {
        TODO("Not yet implemented")
    }


}