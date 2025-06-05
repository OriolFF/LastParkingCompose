package com.uriolus.lastparking.domain.model

data class Parking(
    val id: String,
    val notes: String,
    val location: ParkingLocation,
    val address: String? = null,
    val date: String? = null,
    val imageUri: String? = null
)


val EmptyParking = Parking(
    id = "empty",
    notes = "",
    location = ParkingLocation(0.0, 0.0),
    address = "",
    date = null,
    imageUri = null)
