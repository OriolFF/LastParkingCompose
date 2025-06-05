package com.uriolus.lastparking.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.use_case.GetLastParkingUseCase
import com.uriolus.lastparking.domain.use_case.SaveParkingUseCase
import com.uriolus.lastparking.domain.use_case.GetLocationUpdatesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class MainViewModel(
    private val getLastParkingUseCase: GetLastParkingUseCase,
    private val saveParkingUseCase: SaveParkingUseCase,
    private val getLocationUpdatesUseCase: GetLocationUpdatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    private val _events = MutableSharedFlow<MainViewEvent>()
    val events = _events.asSharedFlow()

    private var locationUpdatesJob: Job? = null

    init {
        handleAction(MainViewAction.LoadLastParking)
    }

    fun handleAction(action: MainViewAction) {
        when (action) {
            is MainViewAction.LoadLastParking -> loadLastParking()
            is MainViewAction.TakePicture -> takePicture()
            is MainViewAction.AddNewParkingClicked -> {
                _uiState.value = MainUiState.RequestingPermission
            }
            is MainViewAction.LocationPermissionResult -> {
                if (action.granted) {
                    startCollectingLocationUpdates()
                } else {
                    _uiState.value = MainUiState.Error(AppError.LocationPermissionDenied)
                }
            }
            is MainViewAction.SaveCurrentLocation -> saveCurrentLocation()
            is MainViewAction.UpdateNotes -> updateNotes(action.notes)
            is MainViewAction.UpdateAddress -> updateAddress(action.address)
            is MainViewAction.UpdateLocation -> updateLocation(action.location)
            is MainViewAction.CancelAddNewParking -> cancelAddNewParking()
        }
    }

    private fun startCollectingLocationUpdates() {
        locationUpdatesJob?.cancel()
        _uiState.value = MainUiState.NewParking(
            parking = EmptyParking,
            gpsAccuracy = null
        )
        locationUpdatesJob = viewModelScope.launch {
            getLocationUpdatesUseCase()
                .catch { e ->
                    _uiState.value = MainUiState.Error(AppError.ErrorLoading("Location Flow Error: ${e.localizedMessage}"))
                }
                .collect { newLocation ->
                    handleAction(MainViewAction.UpdateLocation(newLocation))
                }
        }
    }

    private fun cancelAddNewParking() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
        loadLastParking()
    }

    private fun loadLastParking() {
        _uiState.value = MainUiState.Loading
        viewModelScope.launch {
            when (val result = getLastParkingUseCase.exec()) {
                is Either.Left -> {
                    when (result.value) {
                        AppError.ErrorNoPreviousParking -> _uiState.value = stateForEmptyParking()
                        AppError.LocationPermissionDenied -> _uiState.value = MainUiState.Error(AppError.LocationPermissionDenied)
                        else ->
                            _uiState.value = MainUiState.Error(result.value)
                    }
                }
                is Either.Right -> _uiState.value = stateForLastParkingLoaded(result.value)
            }
        }
    }

    private fun stateForEmptyParking(): MainUiState {
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
        val currentUiState = _uiState.value
        when (currentUiState) {
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

    private fun updateLocation(location: ParkingLocation) {
        val currentUiState = _uiState.value
        if (currentUiState is MainUiState.NewParking) {
            _uiState.value = currentUiState.copy(
                parking = currentUiState.parking.copy(
                    location = ParkingLocation(location.latitude, location.longitude)
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationUpdatesJob?.cancel()
    }
}
