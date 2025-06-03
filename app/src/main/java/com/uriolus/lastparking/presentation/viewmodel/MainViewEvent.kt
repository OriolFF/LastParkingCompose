package com.uriolus.lastparking.presentation.viewmodel

sealed interface MainViewEvent {
    data class ShowMessage(val message: String) : MainViewEvent
    data class NavigateTo(val route: String) : MainViewEvent
}
