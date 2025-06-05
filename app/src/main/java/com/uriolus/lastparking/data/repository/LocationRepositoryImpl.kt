package com.uriolus.lastparking.data.repository

import com.uriolus.lastparking.data.datasource.DataSourceFusedProvider
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocationRepositoryImpl : LocationRepository, KoinComponent {
    private val dataSourceFusedProvider: DataSourceFusedProvider by inject()

    override fun getLocationUpdates(): Flow<ParkingLocation> {
        return dataSourceFusedProvider.getLocationUpdates()
    }
}
