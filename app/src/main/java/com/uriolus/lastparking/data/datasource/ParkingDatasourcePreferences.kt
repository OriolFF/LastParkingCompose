package com.uriolus.lastparking.data.datasource

import arrow.core.Either
import arrow.core.raise.either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingDatasourcePreferences @Inject constructor() : ParkingDatasource {
    
    private var lastParking: Parking? = null
    
    override suspend fun saveParking(parking: Parking): Either<AppError, Unit> = either {
        lastParking = parking
    }
    
    override suspend fun getLastParking(): Either<AppError, Parking> = either {
        lastParking ?: raise(AppError.ErrorLoading("No parking found"))
    }
}
