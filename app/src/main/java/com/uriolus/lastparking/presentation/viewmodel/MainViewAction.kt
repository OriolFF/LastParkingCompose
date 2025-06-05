package com.uriolus.lastparking.presentation.viewmodel

import com.uriolus.lastparking.domain.model.ParkingLocation

sealed interface MainViewAction {
    data object LoadLastParking : MainViewAction
    data object TakePicture : MainViewAction
    data object AddNewParking : MainViewAction
    data object SaveCurrentLocation : MainViewAction
    data class UpdateNotes(val notes: String) : MainViewAction
    data class UpdateAddress(val address: String) : MainViewAction
    data class UpdateLocation(val location: ParkingLocation) : MainViewAction
}
