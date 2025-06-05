package com.uriolus.lastparking.domain.repository

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.ParkingLocation

interface GeocodingRepository {
    suspend fun getAddressFromLocation(location: ParkingLocation): Either<AppError, String?>
}
