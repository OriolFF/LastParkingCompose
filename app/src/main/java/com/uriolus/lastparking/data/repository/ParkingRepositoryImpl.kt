package com.uriolus.lastparking.data.repository

import arrow.core.Either
import com.uriolus.lastparking.data.datasource.ParkingDatasource
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.repository.ParkingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingRepositoryImpl @Inject constructor(
    private val localDataSource: ParkingDatasource
) : ParkingRepository {
    
    override suspend fun saveParking(parking: Parking): Either<AppError, Unit> {
        return localDataSource.saveParking(parking)
    }
    
    override suspend fun getLastParking(): Either<AppError, Parking> {
        return localDataSource.getLastParking()
    }
}
