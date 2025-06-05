package com.uriolus.lastparking.domain.use_case

import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.GeocodingRepository

class GetAddressFromLocationUseCase constructor(
    private val geocodingRepository: GeocodingRepository
) {
    suspend operator fun invoke(location: ParkingLocation): Either<AppError, String?> {
        return geocodingRepository.getAddressFromLocation(location)
    }
}
