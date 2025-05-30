package com.uriolus.lastparking.data.datasource

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking

interface ParkingDatasource {
    suspend fun saveParking(parking: Parking): Either<AppError, Unit>
    suspend fun getLastParking(): Either<AppError, Parking>
}