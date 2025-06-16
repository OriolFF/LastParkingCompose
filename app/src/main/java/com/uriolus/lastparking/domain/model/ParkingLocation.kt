package com.uriolus.lastparking.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ParkingLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null

)
