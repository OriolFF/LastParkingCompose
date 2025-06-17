package com.uriolus.lastparking.di

import com.uriolus.lastparking.domain.use_case.GetAddressFromLocationUseCase
import com.uriolus.lastparking.domain.use_case.GetLastParkingUseCase
import com.uriolus.lastparking.domain.use_case.GetLocationUpdatesUseCase
import com.uriolus.lastparking.domain.use_case.GetMapUrlFromLocationUseCase
import com.uriolus.lastparking.domain.use_case.SaveParkingUseCase
import com.uriolus.lastparking.domain.use_case.DeleteParkingUseCase
import com.uriolus.lastparking.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { GetLastParkingUseCase(get()) }
    single { SaveParkingUseCase(get()) }
    single { DeleteParkingUseCase(get()) }
    single { GetLocationUpdatesUseCase(get()) }
    single { GetAddressFromLocationUseCase(get()) }
    single { GetMapUrlFromLocationUseCase(get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get()) }
}
