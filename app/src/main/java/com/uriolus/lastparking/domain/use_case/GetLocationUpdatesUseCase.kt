package com.uriolus.lastparking.domain.use_case

import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetLocationUpdatesUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<ParkingLocation> {
        // This assumes LocationRepository will have a method like getRealtimeLocationUpdates()
        // which in turn will call DataSourceFusedProvider.getLocationUpdates()
        return repository.getLocationUpdates()
    }
}
