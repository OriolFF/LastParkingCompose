package com.uriolus.lastparking.presentation.viewmodel

import android.net.Uri

sealed interface MainViewAction {
    data object LoadLastParking : MainViewAction
    data object StartNewParkingFlow : MainViewAction // User explicitly wants to start new parking
    data class NewParkingScreenStarted(val imageOutputUri: Uri) : MainViewAction // Screen is ready with URI
    data object ProceedWithInitialNewParking : MainViewAction // Added new action

    // Location Permission related actions
    data object LocationPermissionGranted : MainViewAction
    data object LocationPermissionRequestCancelled : MainViewAction // User cancelled the permission flow (e.g. back press)
    data class LocationPermissionDenied(val shouldShowRationale: Boolean) : MainViewAction
    data object RequestLocationPermissionAgain : MainViewAction // User wants to try granting permission again from rationale
    data object DismissPermissionDialogs : MainViewAction // User dismissed a permission-related dialog

    // Parking data modification actions
    data object CancelAddNewParking : MainViewAction
    data object SaveCurrentLocation : MainViewAction
    data class UpdateNotes(val notes: String) : MainViewAction
    data class UpdateAddress(val address: String) : MainViewAction
    data class SetImageUri(val imageUri: String?) : MainViewAction
    data object DeleteCurrentParking : MainViewAction

    // Camera related actions
    data object ImageClicked : MainViewAction
    data class CameraResult(val success: Boolean) : MainViewAction

}
