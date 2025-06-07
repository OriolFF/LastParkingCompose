package com.uriolus.lastparking.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.use_case.GetAddressFromLocationUseCase
import com.uriolus.lastparking.domain.use_case.GetLastParkingUseCase
import com.uriolus.lastparking.domain.use_case.GetLocationUpdatesUseCase
import com.uriolus.lastparking.domain.use_case.GetMapUrlFromLocationUseCase
import com.uriolus.lastparking.domain.use_case.SaveParkingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val GOOD_ACCURACY_THRESHOLD = 10.0f // meters

class MainViewModel(
    private val getLastParkingUseCase: GetLastParkingUseCase,
    private val saveParkingUseCase: SaveParkingUseCase,
    private val getLocationUpdatesUseCase: GetLocationUpdatesUseCase,
    private val getAddressFromLocationUseCase: GetAddressFromLocationUseCase,
    private val getStaticMapRepositoryUseCase: GetMapUrlFromLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    private val _events = MutableSharedFlow<MainViewEvent>()
    val events = _events.asSharedFlow()

    private var locationUpdatesJob: Job? = null
    private var addressFetchAttempted: Boolean = false

    init {
        handleAction(MainViewAction.LoadLastParking)
    }

    fun handleAction(action: MainViewAction) {
        when (action) {
            is MainViewAction.LoadLastParking -> loadLastParking()
            is MainViewAction.LocationPermissionGranted -> {
                // DO nothing
            }

            is MainViewAction.TakePicture -> { /* TODO: Decide if this action is needed or if UI handles intent directly */
            }

            is MainViewAction.AddNewParkingClicked -> {
                // Directly proceed to get location.
                addressFetchAttempted = false // Reset flag
                _uiState.value = MainUiState.NewParking(
                    parking = EmptyParking.copy(), // Start with a fresh parking object
                    gpsAccuracy = null // Initial accuracy is unknown
                )
                startCollectingLocationUpdates()
            }

            is MainViewAction.LocationPermissionRequestCancelled -> {
                // This might still be relevant if a user cancels a system dialog if that's possible
                // For now, treat as permission not granted, leading to fallback state.
                _uiState.value = MainUiState.PermissionRequiredButNotGranted
                viewModelScope.launch {
                    _events.emit(MainViewEvent.ShowMessage("Location permission request cancelled"))
                }
            }

            is MainViewAction.LocationPermissionDenied -> {
                if (action.shouldShowRationale) {
                    _uiState.value = MainUiState.ShowLocationPermissionRationale
                } else {
                    _uiState.value = MainUiState.ShowLocationPermissionPermanentlyDenied
                }
            }

            is MainViewAction.RequestLocationPermissionAgain -> {
                _uiState.value = MainUiState.RequestingPermission
            }

            is MainViewAction.DismissPermissionDialogs -> {
                _uiState.value = MainUiState.PermissionRequiredButNotGranted
            }

            is MainViewAction.SaveCurrentLocation -> saveCurrentLocation()
            is MainViewAction.UpdateNotes -> updateNotes(action.notes)
            is MainViewAction.UpdateAddress -> updateAddress(action.address)
            is MainViewAction.SetImageUri -> updateImagePath(action.imageUri)
            is MainViewAction.CancelAddNewParking -> cancelAddNewParking()
        }
    }

    private fun startCollectingLocationUpdates() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            getLocationUpdatesUseCase().fold(
                ifLeft = { appError ->
                    when (appError) {
                        is AppError.LocationPermissionDenied -> {
                            _uiState.value = MainUiState.PermissionRequiredButNotGranted
                        }

                        else -> {
                            _uiState.value =
                                MainUiState.Error(
                                    AppError.ErrorLoading("Failed to start location updates: ${appError::class.simpleName}")
                                )
                        }
                    }
                },
                ifRight = { locationFlow ->
                    locationFlow
                        .catch { e -> // Catch errors during flow emission
                            _uiState.value =
                                MainUiState.Error(
                                    AppError.ErrorLoading("Location Flow Error: ${e.localizedMessage}")
                                )
                        }
                        .collect { newLocation ->
                            val currentState = _uiState.value
                            if (currentState is MainUiState.NewParking) {
                                _uiState.value = currentState.copy(
                                    parking = currentState.parking.copy(location = newLocation),
                                    gpsAccuracy = newLocation.accuracy
                                )
                                // Check accuracy and fetch address if needed
                                if (newLocation.accuracy != null &&
                                    newLocation.accuracy <= GOOD_ACCURACY_THRESHOLD &&
                                    !addressFetchAttempted &&
                                    currentState.parking.address.isNullOrEmpty()
                                ) {
                                    addressFetchAttempted = true
                                    fetchAddressForLocation(newLocation)
                                    fetchMapForLocation(newLocation)
                                }
                            }
                        }
                }
            )
        }
    }

    private fun fetchAddressForLocation(location: ParkingLocation) {
        viewModelScope.launch {
            getAddressFromLocationUseCase(location).fold(
                ifLeft = {
                    // Handle error, e.g., show a toast or log
                    _events.emit(MainViewEvent.ShowMessage("Could not fetch address: ${it::class.simpleName}"))
                },
                ifRight = { address ->
                    val currentState = _uiState.value
                    if (currentState is MainUiState.NewParking && address != null) {
                        _uiState.value = currentState.copy(
                            parking = currentState.parking.copy(address = address)
                        )
                    }
                }
            )
        }
    }

    private fun fetchMapForLocation(location: ParkingLocation) {
        viewModelScope.launch {

            getStaticMapRepositoryUseCase(location).fold(
                ifLeft = {
                    // Handle error, e.g., show a toast or log
                    _events.emit(MainViewEvent.ShowMessage("Could not fetch map: ${it::class.simpleName}"))
                },
                ifRight = { mapUrl ->
                    val currentState = _uiState.value
                    if (currentState is MainUiState.NewParking && mapUrl != null) {
                        _uiState.value = currentState.copy(parking = currentState.parking.copy(mapUri = mapUrl))
                    }
                }
            )
        }
    }

    private fun cancelAddNewParking() {
        locationUpdatesJob?.cancel()
        addressFetchAttempted = false // Reset flag
        loadLastParking()
    }

    private fun loadLastParking() {
        _uiState.value = MainUiState.Loading
        viewModelScope.launch {
            when (val result = getLastParkingUseCase.exec()) {
                is Either.Left -> {
                    when (result.value) {
                        AppError.ErrorNoPreviousParking -> _uiState.value =
                            stateForEmptyParking()

                        AppError.LocationPermissionDenied -> _uiState.value =
                            MainUiState.RequestingPermission // Changed to trigger UI permission flow

                        else -> _uiState.value = MainUiState.Error(result.value)
                    }
                }

                is Either.Right -> _uiState.value =
                    stateForLastParkingLoaded(result.value)
            }
        }
    }

    private fun stateForEmptyParking(): MainUiState {
        // If there's no parking, but we also need permission, RequestingPermission state should take precedence.
        // This function is called after permission checks in loadLastParking.
        return MainUiState.Success(
            parking = EmptyParking,
            fabState = FABState(newParking = true, saveParking = false)
        )
    }

    private fun stateForLastParkingLoaded(parking: Parking): MainUiState {
        return MainUiState.Success(
            parking = parking,
            fabState = FABState(newParking = true, saveParking = false)
        )
    }

    private fun saveParking(parking: Parking) = viewModelScope.launch {
        _uiState.value = MainUiState.Loading
        when (val result = saveParkingUseCase(parking)) {
            is Either.Left -> _uiState.value = MainUiState.Error(result.value)
            is Either.Right -> {
                _uiState.value = MainUiState.Success(parking)
                locationUpdatesJob?.cancel()
                locationUpdatesJob = null
            }
        }
    }

    private fun takePicture() {
        viewModelScope.launch {
            _events.emit(MainViewEvent.ShowMessage("Take picture clicked"))
        }
    }

    private fun saveCurrentLocation() {
        val currentState = _uiState.value
        if (currentState is MainUiState.NewParking) {
            saveParking(currentState.parking)
        } else if (currentState is MainUiState.Success && currentState.fabState.saveParking) {
            saveParking(currentState.parking)
        } else {
            viewModelScope.launch {
                _events.emit(MainViewEvent.ShowMessage("No active parking to save."))
            }
        }
    }

    private fun updateNotes(notes: String) {
        val currentState = _uiState.value
        if (currentState is MainUiState.Success) {
            _uiState.value = currentState.copy(
                parking = currentState.parking.copy(notes = notes)
            )
        }
    }

    private fun updateAddress(address: String) {
        when (val currentUiState = _uiState.value) {
            is MainUiState.Success -> {
                _uiState.value = currentUiState.copy(
                    parking = currentUiState.parking.copy(address = address),
                    fabState = currentUiState.fabState.copy(saveParking = true)
                )
            }

            is MainUiState.NewParking -> {
                _uiState.value = currentUiState.copy(
                    parking = currentUiState.parking.copy(address = address)
                )
            }

            else -> {}
        }
    }

    private fun updateImagePath(imageUri: String?) {
        val currentState = _uiState.value
        if (currentState is MainUiState.NewParking) {
            _uiState.value = currentState.copy(
                parking = currentState.parking.copy(imageUri = imageUri)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationUpdatesJob?.cancel()
    }
}
