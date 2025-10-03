package com.psis.elimlift.presentation.contracts

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract

class EnableLocationContract(private val context: Context) :
    ActivityResultContract<Unit, Boolean>() {
    override fun createIntent(context: Context, input: Unit) =
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

