package com.uriolus.lastparking.domain.use_case

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.repository.ParkingRepository

class SaveParkingUseCase(
    private val repository: ParkingRepository
) {
    suspend operator fun invoke(parking: Parking): Either<AppError, Unit> {
        return repository.saveParking(parking)
    }
}
