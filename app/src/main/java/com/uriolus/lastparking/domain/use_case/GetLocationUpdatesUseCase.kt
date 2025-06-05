package com.uriolus.lastparking.domain.use_case

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetLocationUpdatesUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(): Either<AppError, Flow<ParkingLocation>> {
        return try {
            val locationFlow = repository.getLocationUpdates()
            Either.Right(locationFlow)
        } catch (e: SecurityException) {
            Either.Left(AppError.LocationPermissionDenied)
        } catch (e: Exception) {
            Either.Left(AppError.ErrorLoading("Failed to initiate location updates: ${e.message ?: "Unknown error"}"))
        }
    }
}
