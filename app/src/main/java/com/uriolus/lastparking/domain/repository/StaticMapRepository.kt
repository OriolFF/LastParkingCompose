package com.uriolus.lastparking.domain.repository

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.ParkingLocation

interface StaticMapRepository {
    fun getMapForLocation(parkingLocation: ParkingLocation): Either<AppError, String>
}