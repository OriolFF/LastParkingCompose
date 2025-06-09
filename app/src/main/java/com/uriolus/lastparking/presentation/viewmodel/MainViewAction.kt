package com.uriolus.lastparking.presentation.viewmodel

import android.net.Uri

sealed interface MainViewAction {
    data class NewParkingScreenStarted(val imageOutputUri: Uri) : MainViewAction
    data object StartNewParkingFlow : MainViewAction
    data object LocationPermissionRequestCancelled : MainViewAction
    data class LocationPermissionDenied(val shouldShowRationale: Boolean) : MainViewAction
    data object RequestLocationPermissionAgain : MainViewAction
    data object LocationPermissionGranted : MainViewAction
    data object DismissPermissionDialogs : MainViewAction
    data object CancelAddNewParking : MainViewAction
    data object LoadLastParking : MainViewAction
    data object TakePicture : MainViewAction
    data class CameraResult(val success: Boolean) : MainViewAction

    data object SaveCurrentLocation : MainViewAction
    data class UpdateNotes(val notes: String) : MainViewAction
    data class UpdateAddress(val address: String) : MainViewAction
    data class SetImageUri(val imageUri: String?) : MainViewAction

}
