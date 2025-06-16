package com.uriolus.lastparking.data.datasource

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.uriolus.lastparking.domain.model.ParkingLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DataSourceFusedProvider : KoinComponent {
    private val fusedLocationProviderClient: FusedLocationProviderClient by inject()

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<ParkingLocation> = callbackFlow @androidx.annotation.RequiresPermission(
        allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
    ) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(
                        ParkingLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = if (location.hasAccuracy()) location.accuracy else null
                        )
                    )
                }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()

        fusedLocationProviderClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e)
        }

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }
}
