package com.uriolus.lastparking.di

import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
    // Add application-wide Koin bindings here
}
