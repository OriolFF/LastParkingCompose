package com.uriolus.lastparking.domain.repository

import com.uriolus.lastparking.domain.model.ParkingLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationUpdates(): Flow<ParkingLocation>
}
