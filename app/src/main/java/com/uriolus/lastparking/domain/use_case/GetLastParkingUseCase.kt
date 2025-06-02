package com.uriolus.lastparking.domain.use_case

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.repository.ParkingRepository

class GetLastParkingUseCase(
    private val repository: ParkingRepository
) {
    suspend fun exec(): Either<AppError, Parking> {
        return repository.getLastParking()
    }
}
