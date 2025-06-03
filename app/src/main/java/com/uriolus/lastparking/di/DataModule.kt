package com.uriolus.lastparking.di

import com.uriolus.lastparking.data.datasource.ParkingDatasource
import com.uriolus.lastparking.data.datasource.ParkingDatasourceMock
import com.uriolus.lastparking.data.datasource.ParkingDatasourcePreferences
import com.uriolus.lastparking.data.repository.ParkingRepositoryImpl
import com.uriolus.lastparking.domain.repository.ParkingRepository
import org.koin.dsl.module

val dataModule = module {
    single<ParkingDatasource> { //ParkingDatasourcePreferences(get())
        ParkingDatasourceMock()
    }
    single<ParkingRepository> { ParkingRepositoryImpl(get()) }
}
