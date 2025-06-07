package com.uriolus.lastparking.di

import com.uriolus.lastparking.data.datasource.DataSourceFusedProvider
import com.uriolus.lastparking.data.datasource.ParkingDatasource
import com.uriolus.lastparking.data.datasource.ParkingDatasourceMock
import com.uriolus.lastparking.data.repository.GeocodingRepositoryImpl
import com.uriolus.lastparking.data.repository.LocationRepositoryImpl
import com.uriolus.lastparking.data.repository.ParkingRepositoryImpl
import com.uriolus.lastparking.data.repository.StaticMapGooleRepository
import com.uriolus.lastparking.domain.repository.GeocodingRepository
import com.uriolus.lastparking.domain.repository.LocationRepository
import com.uriolus.lastparking.domain.repository.ParkingRepository
import com.uriolus.lastparking.domain.repository.StaticMapRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // Data sources
    single<ParkingDatasource> { ParkingDatasourceMock() }

    val KEY: String = "AIzaSyDjXx1VcBDek6A0UPGQqIduxjgVcbJ_sEA"


    // Repositories
    singleOf(::ParkingRepositoryImpl) bind ParkingRepository::class
    singleOf(::LocationRepositoryImpl) bind LocationRepository::class
    single<GeocodingRepository> { GeocodingRepositoryImpl(get()) }
    single<StaticMapRepository> { StaticMapGooleRepository(KEY) }

    // Services/Providers
    single { DataSourceFusedProvider() }
}
