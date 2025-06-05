package com.uriolus.lastparking.domain.model

data class ParkingLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null

)
