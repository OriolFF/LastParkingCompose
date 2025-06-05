package com.uriolus.lastparking.di

import com.uriolus.lastparking.data.datasource.DataSourceFusedProvider
import com.uriolus.lastparking.data.datasource.ParkingDatasource
import com.uriolus.lastparking.data.datasource.ParkingDatasourceMock
import com.uriolus.lastparking.data.repository.GeocodingRepositoryImpl
import com.uriolus.lastparking.data.repository.LocationRepositoryImpl
import com.uriolus.lastparking.data.repository.ParkingRepositoryImpl
import com.uriolus.lastparking.domain.repository.GeocodingRepository
import com.uriolus.lastparking.domain.repository.LocationRepository
import com.uriolus.lastparking.domain.repository.ParkingRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // Data sources
    single<ParkingDatasource> { ParkingDatasourceMock() }
    
    // Repositories
    singleOf(::ParkingRepositoryImpl) bind ParkingRepository::class
    singleOf(::LocationRepositoryImpl) bind LocationRepository::class
    single<GeocodingRepository> { GeocodingRepositoryImpl(get()) }
    
    // Services/Providers
    single { DataSourceFusedProvider() }
}
