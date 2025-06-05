package com.uriolus.lastparking.domain.repository

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking

interface ParkingRepository {
    suspend fun saveParking(parking: Parking): Either<AppError, Unit>
    suspend fun getLastParking(): Either<AppError, Parking>
}
