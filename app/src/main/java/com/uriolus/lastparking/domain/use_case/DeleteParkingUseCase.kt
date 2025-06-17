package com.uriolus.lastparking.domain.use_case

import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.repository.ParkingRepository

class DeleteParkingUseCase(private val repository: ParkingRepository) {
    suspend fun exec(parking: Parking) {
        repository.deleteParking(parking)
    }
}
