package com.uriolus.lastparking.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.use_case.GetLastParkingUseCase
import com.uriolus.lastparking.domain.use_case.SaveParkingUseCase
import com.uriolus.lastparking.presentation.contract.MainAction
import com.uriolus.lastparking.presentation.contract.MainEvent
import com.uriolus.lastparking.presentation.viewstate.FABState
import com.uriolus.lastparking.presentation.viewstate.MainUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class MainViewModel(
    private val getLastParkingUseCase: GetLastParkingUseCase,
    private val saveParkingUseCase: SaveParkingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    private val _events = MutableSharedFlow<MainEvent>()
    val events = _events.asSharedFlow()

    init {
        handleAction(MainAction.LoadLastParking)
    }

    fun handleAction(action: MainAction) {
        when (action) {
            is MainAction.LoadLastParking -> loadLastParking()
            is MainAction.TakePicture -> takePicture()
            is MainAction.AddNewLocation -> addNewLocation()
            is MainAction.SaveCurrentLocation -> saveCurrentLocation()
            is MainAction.UpdateNotes -> updateNotes(action.notes)
            is MainAction.UpdateAddress -> updateAddress(action.address)
        }
    }

    private fun loadLastParking() {
        _uiState.value = MainUiState.Loading
        viewModelScope.launch {
            when (val result = getLastParkingUseCase.exec()) {
                is Either.Left -> {
                    println("Error getting last parking: ${result.value}")
                    when (result.value) {
                        AppError.ErrorNoPreviousParking -> _uiState.value = stateForEmptyParking()
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
            hasChanges = false,
            fabState = FABState(newParking = true, saveParking = false)
        )

    }

    private fun stateForLastParkingLoaded(parking: Parking): MainUiState {
        return MainUiState.Success(
            parking = parking,
            hasChanges = false,
            fabState = FABState(newParking = true, saveParking = false)
        )
    }

    private fun saveParking(parking: Parking) = viewModelScope.launch {
        _uiState.value = MainUiState.Loading
        when (val result = saveParkingUseCase(parking)) {
            is Either.Left -> _uiState.value = MainUiState.Error(result.value)
            is Either.Right -> _uiState.value = MainUiState.Success(parking)
        }
    }

    private fun takePicture() {
        viewModelScope.launch {
            _events.emit(MainEvent.ShowMessage("Take picture clicked"))
        }
    }

    private fun addNewLocation() {
        viewModelScope.launch {
            _events.emit(MainEvent.ShowMessage("Add new location clicked"))
        }
    }

    private fun saveCurrentLocation() {
        viewModelScope.launch {
            _events.emit(MainEvent.ShowMessage("Save current location clicked"))
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
        val currentState = _uiState.value
        if (currentState is MainUiState.Success) {
            _uiState.value = currentState.copy(
                parking = currentState.parking.copy(address = address)
            )
        }
    }
}
