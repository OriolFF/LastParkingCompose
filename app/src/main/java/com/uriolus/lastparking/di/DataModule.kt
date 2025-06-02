package com.uriolus.lastparking.di

import com.uriolus.lastparking.data.datasource.ParkingDatasource
import com.uriolus.lastparking.data.datasource.ParkingDatasourcePreferences
import com.uriolus.lastparking.data.repository.ParkingRepositoryImpl
import com.uriolus.lastparking.domain.repository.ParkingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindParkingLocalDataSource(
        impl: ParkingDatasourcePreferences
    ): ParkingDatasource

    @Binds
    @Singleton
    abstract fun bindParkingRepository(
        impl: ParkingRepositoryImpl
    ): ParkingRepository
}
