package com.uriolus.lastparking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.use_case.GetLastParkingUseCase
import com.uriolus.lastparking.domain.use_case.SaveParkingUseCase
import com.uriolus.lastparking.presentation.viewstate.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getLastParkingUseCase: GetLastParkingUseCase,
    private val saveParkingUseCase: SaveParkingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Empty)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun loadLastParking() = viewModelScope.launch {
        _uiState.value = MainUiState.Loading
        when (val result = getLastParkingUseCase()) {
            is Either.Left -> _uiState.value = MainUiState.Error(result.value)
            is Either.Right -> _uiState.value = MainUiState.Success(result.value)
        }
    }

    fun saveParking(parking: Parking) = viewModelScope.launch {
        _uiState.value = MainUiState.Loading
        when (val result = saveParkingUseCase(parking)) {
            is Either.Left -> _uiState.value = MainUiState.Error(result.value)
            is Either.Right -> _uiState.value = MainUiState.Success(parking)
        }
    }
}
