package com.uriolus.lastparking.presentation.viewmodel

import com.uriolus.lastparking.domain.model.ParkingLocation

sealed interface MainViewAction {
    data object AddNewParkingClicked : MainViewAction
    data object LocationPermissionRequestCancelled : MainViewAction
    data class LocationPermissionDenied(val shouldShowRationale: Boolean) : MainViewAction
    data object RequestLocationPermissionAgain : MainViewAction
    data object DismissPermissionDialogs : MainViewAction
    data object CancelAddNewParking : MainViewAction
    data object LoadLastParking : MainViewAction
    data object TakePicture : MainViewAction // This might be replaced or used by the button before launching the intent

    data object SaveCurrentLocation : MainViewAction
    data class UpdateNotes(val notes: String) : MainViewAction
    data class UpdateAddress(val address: String) : MainViewAction
    data class ImagePathUpdated(val imageUri: String?) : MainViewAction
    data object LocationPermissionGranted : MainViewAction
}
