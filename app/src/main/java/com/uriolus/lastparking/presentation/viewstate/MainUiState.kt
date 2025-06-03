package com.uriolus.lastparking.presentation.viewstate

import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking

sealed class MainUiState {
    data object Loading : MainUiState()
    data class Error(val error: AppError) : MainUiState()
    data class Success(
        val parking: Parking,
        val hasChanges: Boolean = false,
        val fabState: FABState = FABState()
    ) : MainUiState()
}

data class FABState(val newParking: Boolean = false, val saveParking: Boolean = false)
