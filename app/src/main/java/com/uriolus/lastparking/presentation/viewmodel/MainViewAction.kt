package com.uriolus.lastparking.presentation.viewmodel

import com.uriolus.lastparking.domain.model.ParkingLocation

sealed interface MainViewAction {
    // Renaming AddNewParking to be more explicit about user intent vs. internal state change
    data object AddNewParkingClicked : MainViewAction 
    data class LocationPermissionResult(val granted: Boolean) : MainViewAction
    data object CancelAddNewParking : MainViewAction
    data object LoadLastParking : MainViewAction
    data object TakePicture : MainViewAction

    data object SaveCurrentLocation : MainViewAction
    data class UpdateNotes(val notes: String) : MainViewAction
    data class UpdateAddress(val address: String) : MainViewAction
    data class UpdateLocation(val location: ParkingLocation) : MainViewAction
}
