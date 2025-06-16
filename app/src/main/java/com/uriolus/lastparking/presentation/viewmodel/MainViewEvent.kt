package com.uriolus.lastparking.presentation.viewmodel

import android.net.Uri
import com.uriolus.lastparking.domain.model.AppError

sealed interface MainViewEvent {
    data class ShowMessage(val message: String) : MainViewEvent
    data class ShowError(val error: AppError) : MainViewEvent
    data class NavigateTo(val route: String) : MainViewEvent
    data class TakeAPicture(val uriImage: Uri) : MainViewEvent
}
