package com.uriolus.lastparking.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Parking(
    val id: String,
    val notes: String,
    val location: ParkingLocation?=null,
    val address: String? = null,
    val date: String? = null,
    val imageUri: String? = null,
    val mapUri: String? = null,
    val timestamp: Long
)


val EmptyParking = Parking(
    id = "empty",
    notes = "",
    location = ParkingLocation(0.0, 0.0),
    address = "",
    date = null,
    imageUri = null,
    timestamp = System.currentTimeMillis()
)
