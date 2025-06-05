package com.uriolus.lastparking.di

import com.uriolus.lastparking.presentation.viewmodel.MainViewModel
import com.uriolus.lastparking.domain.use_case.GetLastParkingUseCase
import com.uriolus.lastparking.domain.use_case.SaveParkingUseCase
import com.uriolus.lastparking.domain.use_case.GetLocationUpdatesUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { GetLastParkingUseCase(get()) }
    single { SaveParkingUseCase(get()) }
    single { GetLocationUpdatesUseCase(get()) }
    viewModel { MainViewModel(get(), get(), get()) }
}
