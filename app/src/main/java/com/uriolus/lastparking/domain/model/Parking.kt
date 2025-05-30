package com.uriolus.lastparking.domain.model

data class Parking(
    val id: String,
    val notes: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val date: String? = null,
    val imageUri: String? = null
)