package com.uriolus.lastparking.data.repository

import arrow.core.Either
import com.uriolus.lastparking.data.datasource.ParkingDatasource
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.repository.ParkingRepository

class ParkingRepositoryImpl(
    private val localDataSource: ParkingDatasource
) : ParkingRepository {
    
    override suspend fun saveParking(parking: Parking): Either<AppError, Unit> {
        return localDataSource.saveParking(parking)
    }
    
    override suspend fun getLastParking(): Either<AppError, Parking> {
        return localDataSource.getParking()
    }

    override suspend fun deleteParking(parking: Parking): Either<AppError, Unit> {
        return localDataSource.deleteParking(parking)
    }
}
