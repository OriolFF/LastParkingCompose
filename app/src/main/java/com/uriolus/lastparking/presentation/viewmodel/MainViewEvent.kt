package com.uriolus.lastparking.presentation.viewmodel

import com.uriolus.lastparking.domain.model.AppError

sealed interface MainViewEvent {
    data class ShowMessage(val message: String) : MainViewEvent
    data class ShowError(val error: AppError) : MainViewEvent
    data class TakeAPicture(val uriImage: String) : MainViewEvent
}
