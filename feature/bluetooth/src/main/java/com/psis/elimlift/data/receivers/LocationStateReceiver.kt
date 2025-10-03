package com.psis.elimlift.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager

class LocationStateReceiver(private val onLocationStateChanged: (Boolean) -> Unit) :
    BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isLocationEnabled =
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            onLocationStateChanged(isLocationEnabled)
        }
    }
}