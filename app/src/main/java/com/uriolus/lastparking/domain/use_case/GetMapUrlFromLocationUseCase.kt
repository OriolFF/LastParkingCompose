package com.uriolus.lastparking.domain.use_case

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.GeocodingRepository
import com.uriolus.lastparking.domain.repository.StaticMapRepository

class GetMapUrlFromLocationUseCase constructor(
    private val staticMapRepository: StaticMapRepository
) {
    suspend operator fun invoke(location: ParkingLocation): Either<AppError, String?> {
        return staticMapRepository.getMapForLocation(location)
    }
}
