package com.uriolus.lastparking.data.repository

import android.os.Debug
import android.util.Log
import arrow.core.Either
import arrow.core.right
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.StaticMapRepository

class StaticMapGooleRepository(val key: String) : StaticMapRepository {

    override fun getMapForLocation(parkingLocation: ParkingLocation): Either<AppError, String> {

        val lati = parkingLocation.latitude
        val longi = parkingLocation.longitude
        val URI = "https://maps.google.com/maps/api/staticmap?markers=color:blue%7Clabel:P%7" +
                "C$lati,$longi&zoom=17&size=640x300&&scale=1&sensor=false&key=$key"
        Log.d("URI","UriRequest: $URI")
        return URI.right()
    }
}
/*
42.09580122394702, 1.831031025657379
"http://maps.google.com/maps/api/staticmap?markers=color:blue%7Clabel:P%7C42.09580122394702,1.831031025657379&zoom=17&size=640x300&&scale=1&sensor=false&key=AIzaSyDjXx1VcBDek6A0UPGQqIduxjgVcbJ_sEA"

 */