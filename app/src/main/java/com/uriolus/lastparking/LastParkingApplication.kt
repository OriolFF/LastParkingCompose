package com.uriolus.lastparking

import android.app.Application
import com.uriolus.lastparking.di.appModule
import com.uriolus.lastparking.di.dataModule
import com.uriolus.lastparking.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LastParkingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LastParkingApplication)
            modules(appModule, dataModule, viewModelModule)
        }
    }
}
