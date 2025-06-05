package com.uriolus.lastparking.data.datasource

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationResult
import com.uriolus.lastparking.domain.model.ParkingLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DataSourceFusedProvider : KoinComponent {
    private val fusedLocationProviderClient: FusedLocationProviderClient by inject()

    fun getLocationUpdates(): Flow<ParkingLocation> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(
                        ParkingLocation(
                            latitude = location.latitude,
                            longitude = location.longitude
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
            android.os.Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e)
        }

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }
}
