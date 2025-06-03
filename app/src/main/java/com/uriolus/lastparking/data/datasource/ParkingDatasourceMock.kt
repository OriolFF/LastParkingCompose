package com.uriolus.lastparking.data.datasource

import arrow.core.Either
import arrow.core.right
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import kotlinx.coroutines.delay

class ParkingDatasourceMock : ParkingDatasource {
    override suspend fun saveParking(parking: Parking): Either<AppError, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getLastParking(): Either<AppError, Parking> {
        delay(1000L) // Simulate a delayR()
        return EmptyParking.copy(
            address = "Pla de l'alemany 17",
            latitude = 41.403,
            longitude = 2.174,
            notes = "Mis notas"
        ).right()
    }
}