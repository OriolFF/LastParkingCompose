package com.uriolus.lastparking.domain.use_case

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.repository.ParkingRepository
import javax.inject.Inject

class GetLastParkingUseCase @Inject constructor(
    private val repository: ParkingRepository
) {
    suspend operator fun invoke(): Either<AppError, Parking> {
        return repository.getLastParking()
    }
}
