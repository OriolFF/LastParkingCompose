package com.uriolus.lastparking.di

import com.uriolus.lastparking.data.datasource.DataSourceFusedProvider
import com.uriolus.lastparking.data.datasource.ParkingDatasource
import com.uriolus.lastparking.data.datasource.ParkingDatasourceMock
import com.uriolus.lastparking.data.datasource.ParkingDatasourcePreferences
import com.uriolus.lastparking.data.repository.LocationRepositoryImpl
import com.uriolus.lastparking.data.repository.ParkingRepositoryImpl
import com.uriolus.lastparking.domain.repository.LocationRepository
import com.uriolus.lastparking.domain.repository.ParkingRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    // Data sources
    single<ParkingDatasource> { ParkingDatasourceMock() }
    
    // Repositories
    single<LocationRepository> { LocationRepositoryImpl() }
    single<ParkingRepository> { ParkingRepositoryImpl(get()) }
    
    // Services/Providers
    single { DataSourceFusedProvider() }
}
