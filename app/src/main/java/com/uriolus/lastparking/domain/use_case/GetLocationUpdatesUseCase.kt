package com.uriolus.lastparking.domain.use_case

import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetLocationUpdatesUseCase(
    private val repository: LocationRepository
) {
    fun exec(): Flow<ParkingLocation> =
      repository.getLocationUpdates()


}
