package com.uriolus.lastparking.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.GeocodingRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


class GeocodingRepositoryImpl constructor(
    private val context: Context
) : GeocodingRepository {

    private val geocoder = Geocoder(context)

    override suspend fun getAddressFromLocation(location: ParkingLocation): Either<AppError, String?> {
        if (!Geocoder.isPresent()) {
            return Either.Left(AppError.GeocoderNotAvailable)
        }
        return withContext(Dispatchers.IO) {
            try {
                val addresses: List<Address>?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var attempts = 0
                    var result: List<Address>? = null
                    while (attempts < 3 && result == null) {
                        try {
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) { foundAddresses ->
                                result = foundAddresses
                            }
                            // Wait a bit for the async callback, with a timeout
                            // This is a simplified wait; a more robust solution might use a CompletableFuture or similar.
                            for (i in 0..9) { // Max 1 second wait (10 * 100ms)
                                if (result != null) break
                                kotlinx.coroutines.delay(100) // Non-blocking delay
                            }

                        } catch (e: IOException) {
                            // Potentially log and retry or just let it be null for the next attempt
                        }
                        attempts++
                    }
                    addresses = result
                } else {
                    @Suppress("DEPRECATION")
                    addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }

                if (addresses.isNullOrEmpty()) {
                    Either.Left(AppError.NoAddressFound)
                } else {
                    val address = addresses[0]
                    val addressText = address.getAddressLine(0) // Or build from specific fields
                    Either.Right(addressText)
                }
            } catch (e: IOException) {
                // Network or other I/O issues
                Either.Left(AppError.GeocodingIOError(e.localizedMessage ?: "Geocoding I/O error"))
            } catch (e: IllegalArgumentException) {
                // Invalid latitude or longitude values
                Either.Left(AppError.GeocodingIllegalArgument(e.localizedMessage ?: "Invalid coordinates for geocoding"))
            }
        }
    }
}
