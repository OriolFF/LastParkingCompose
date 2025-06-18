package com.uriolus.lastparking.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.use_case.DeleteParkingUseCase
import com.uriolus.lastparking.domain.use_case.GetAddressFromLocationUseCase
import com.uriolus.lastparking.domain.use_case.GetLastParkingUseCase
import com.uriolus.lastparking.domain.use_case.GetLocationUpdatesUseCase
import com.uriolus.lastparking.domain.use_case.GetMapUrlFromLocationUseCase
import com.uriolus.lastparking.domain.use_case.SaveParkingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

private const val GOOD_ACCURACY_THRESHOLD = 50.0f // meters

class MainViewModel(
    private val getLastParkingUseCase: GetLastParkingUseCase,
    private val saveParkingUseCase: SaveParkingUseCase,
    private val deleteParkingUseCase: DeleteParkingUseCase,
    private val getLocationUpdatesUseCase: GetLocationUpdatesUseCase,
    private val getAddressFromLocationUseCase: GetAddressFromLocationUseCase,
    private val getStaticMapRepositoryUseCase: GetMapUrlFromLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainViewEvent>()
    val events: SharedFlow<MainViewEvent> = _events.asSharedFlow()

    private var locationUpdatesJob: Job? = null
    private var addressFetchAttempted: Boolean = false
    private var newParkingImageOutputUri: String? = null

    init {
        handleAction(MainViewAction.LoadLastParking)
    }

    fun handleAction(action: MainViewAction) {
        when (action) {
            is MainViewAction.LoadLastParking -> loadLastParking()
            is MainViewAction.StartNewParkingFlow -> startNewParkingLocationUpdates(isInitialFlow = false)
            is MainViewAction.ProceedWithInitialNewParking -> startNewParkingLocationUpdates(isInitialFlow = true)
            is MainViewAction.NewParkingScreenStarted -> {
                newParkingImageOutputUri = action.imageOutputUri
            }
            is MainViewAction.LocationPermissionGranted -> startNewParkingLocationUpdates(isInitialFlow = true)
            is MainViewAction.LocationPermissionRequestCancelled -> loadLastParking()
            is MainViewAction.LocationPermissionDenied -> {
                if (action.shouldShowRationale) {
                    _uiState.update { MainUiState.ShowLocationPermissionRationale }
                } else {
                    _uiState.update { MainUiState.ShowLocationPermissionPermanentlyDenied }
                }
            }
            is MainViewAction.RequestLocationPermissionAgain -> _uiState.update { MainUiState.RequestingPermission }
            is MainViewAction.DismissPermissionDialogs -> {
                val previousState = _uiState.value
                if (previousState !is MainUiState.Success && previousState !is MainUiState.NewParking) {
                    loadLastParking()
                } else {
                    _uiState.update { previousState }
                }
            }
            is MainViewAction.CancelAddNewParking -> {
                locationUpdatesJob?.cancel()
                addressFetchAttempted = false
                newParkingImageOutputUri = null
                loadLastParking()
            }
            is MainViewAction.SaveCurrentLocation -> saveCurrentLocation()
            is MainViewAction.UpdateNotes -> updateParkingDetails { it.copy(notes = action.notes) }
            is MainViewAction.UpdateAddress -> updateParkingDetails { it.copy(address = action.address) }
            is MainViewAction.SetImageUri -> updateParkingDetails { it.copy(imageUri = action.imageUri) }
            is MainViewAction.ImageClicked -> onTakePicture()
            is MainViewAction.CameraResult -> handleCameraResult(action.success)
            is MainViewAction.DeleteCurrentParking -> handleDeleteCurrentParking()
            is MainViewAction.WalkToLocation -> {
                val currentState = _uiState.value
                if (currentState is MainUiState.Success) {
                    currentState.parking.location?.let { location ->
                        viewModelScope.launch {
                            _events.emit(MainViewEvent.OnWalkToLocation(location))
                        }
                    }
                }
            }
        }
    }

    private fun startNewParkingLocationUpdates(isInitialFlow: Boolean) {
        locationUpdatesJob?.cancel()
        addressFetchAttempted = false

        _uiState.update { MainUiState.NewParking(parking = EmptyParking.copy(timestamp = System.currentTimeMillis()), gpsAccuracy = null, isInitialFlow = isInitialFlow) }

        locationUpdatesJob = viewModelScope.launch {
            getLocationUpdatesUseCase.exec()
                .collect { locationResult ->

                            val currentParkingState = _uiState.value as? MainUiState.NewParking ?: return@collect

                            val updatedParking = currentParkingState.parking.copy(location = locationResult)
                            _uiState.update {
                                currentParkingState.copy(
                                    parking = updatedParking,
                                    gpsAccuracy = locationResult.accuracy
                                )
                            }
                            Log.d("MainViewModel", "Location updates: $locationResult, accuracy: ${locationResult.accuracy}")
                            if (locationResult.accuracy != null &&
                                locationResult.accuracy <= GOOD_ACCURACY_THRESHOLD && !addressFetchAttempted) {
                                addressFetchAttempted = true
                                fetchAddressAndMap(locationResult, updatedParking, currentParkingState)
                            }
                }
        }
    }


    private fun onTakePicture() {
        Log.d("MainViewModel", "onTakePicture called. newParkingImageOutputUri: $newParkingImageOutputUri")
        newParkingImageOutputUri?.let { uriString ->
            viewModelScope.launch { 
                Log.d("MainViewModel", "Emitting TakeAPicture event with URI: $uriString")
                _events.emit(MainViewEvent.TakeAPicture(uriString))
            }
        } ?: Log.d("MainViewModel", "newParkingImageOutputUri is null, not emitting TakeAPicture event.")
    }

    private fun handleCameraResult(success: Boolean) {
        val imageUriTaken = newParkingImageOutputUri

        val currentState = _uiState.value
        var targetParkingState: MainUiState.NewParking? = null

        if (currentState is MainUiState.NewParking) {
            targetParkingState = currentState
        }

        if (targetParkingState != null) {
            if (success && imageUriTaken != null) {
                _uiState.update {
                    targetParkingState.copy(
                        parking = targetParkingState.parking.copy(imageUri = imageUriTaken)
                    )
                }
            } else {
                _uiState.update { targetParkingState }
                if (!success) {
                    viewModelScope.launch {
                        _events.emit(MainViewEvent.ShowError(AppError.ErrorTakingPicture))
                    }
                }
            }
        } else {
            loadLastParking()
        }
    }

    private fun loadLastParking() {
        _uiState.update { MainUiState.Loading }
        viewModelScope.launch {
            when (val result = getLastParkingUseCase.exec()) {
                is Either.Right -> {
                    if (result.value == EmptyParking) {
                        _uiState.update { MainUiState.InitialNewParkingRequiresPermissionCheck }
                    } else {
                        _uiState.update { MainUiState.Success(result.value) }
                    }
                }
                is Either.Left -> {
                    _uiState.update { MainUiState.InitialNewParkingRequiresPermissionCheck }
                }
            }
        }
    }

    private fun fetchAddressAndMap(
        location: ParkingLocation,
        updatedParking: Parking,
        currentParkingState: MainUiState.NewParking
    ) {
        viewModelScope.launch {
            val addressResult = getAddressFromLocationUseCase(location)
            val mapUrlResult = getStaticMapRepositoryUseCase(location)

            val finalParking = updatedParking.copy(
                address = if (addressResult is Either.Right) addressResult.value else "",
                mapUri = if (mapUrlResult is Either.Right) mapUrlResult.value else null
            )

            _uiState.update {
                currentParkingState.copy(parking = finalParking)
            }

            if (addressResult is Either.Left) {
                _events.emit(MainViewEvent.ShowError(addressResult.value))
            }
            if (mapUrlResult is Either.Left) {
                _events.emit(MainViewEvent.ShowError(mapUrlResult.value))
            }
        }
    }

    private fun saveCurrentLocation() {
        val parkingToSave = when (val currentState = _uiState.value) {
            is MainUiState.NewParking -> currentState.parking
            is MainUiState.Success -> currentState.parking
            else -> null
        }

        if (parkingToSave != null) {
            viewModelScope.launch {
                when (val result = saveParkingUseCase.exec(parkingToSave)) {
                    is Either.Right -> {
                        newParkingImageOutputUri = null
                        _uiState.update { MainUiState.Success(parkingToSave) }
                        _events.emit(MainViewEvent.ShowMessage("Location Saved"))
                    }

                    is Either.Left -> _events.emit(MainViewEvent.ShowError(result.value))
                }
            }
        }
    }

    private fun updateParkingDetails(updateLogic: (Parking) -> Parking) {
        val currentState = _uiState.value
        if (currentState is MainUiState.NewParking) {
            _uiState.update { currentState.copy(parking = updateLogic(currentState.parking)) }
        } else if (currentState is MainUiState.Success) {
            val updatedParking = updateLogic(currentState.parking)
            _uiState.update { MainUiState.Success(updatedParking) }
        }
    }

    private fun handleDeleteCurrentParking() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MainUiState.Success) {
                deleteParkingUseCase.exec(currentState.parking)
                checkLastParking()
            }
        }
    }

    private fun checkLastParking() {
        viewModelScope.launch {
            when (val result = getLastParkingUseCase.exec()) {
                is Either.Right -> {
                    if (result.value == EmptyParking) {
                        _uiState.update { MainUiState.InitialNewParkingRequiresPermissionCheck }
                    } else {
                        _uiState.update { MainUiState.Success(result.value) }
                    }
                }
                is Either.Left -> {
                    _uiState.update { MainUiState.InitialNewParkingRequiresPermissionCheck }
                }
            }
        }
    }
}
