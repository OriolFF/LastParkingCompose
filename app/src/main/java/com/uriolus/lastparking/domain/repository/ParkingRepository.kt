package com.uriolus.lastparking.domain.repository

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import kotlinx.coroutines.flow.Flow

interface ParkingRepository {
    suspend fun saveParking(parking: Parking): Either<AppError, Unit>
    suspend fun getLastParking(): Either<AppError, Parking>
}
