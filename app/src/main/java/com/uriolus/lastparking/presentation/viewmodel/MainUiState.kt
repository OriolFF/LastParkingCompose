package com.uriolus.lastparking.presentation.viewmodel

import android.net.Uri
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking

sealed class MainUiState {
    data object RequestingPermission : MainUiState()
    data object Loading : MainUiState()
    data class Error(val error: AppError) : MainUiState()
    data class Success(
        val parking: Parking,
        val fabState: FABState = FABState(newParking = true)
    ) : MainUiState()

    data class NewParking(
        val parking: Parking, // This will be updated with live location
        val fabState: FABState = FABState(newParking = true, saveParking = true), // Enable save by default
        val gpsAccuracy: Float? // Can be updated with accuracy from location updates
    ) : MainUiState()

    // New states for permission dialogs
    data object ShowLocationPermissionRationale : MainUiState()
    data object ShowLocationPermissionPermanentlyDenied : MainUiState()
    data object PermissionRequiredButNotGranted : MainUiState() // Fallback state
    data object InitialNewParkingRequiresPermissionCheck : MainUiState()

}

data class FABState(val newParking: Boolean = false, val saveParking: Boolean = false)
