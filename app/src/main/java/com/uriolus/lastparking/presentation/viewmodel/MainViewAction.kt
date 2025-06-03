package com.uriolus.lastparking.presentation.viewmodel

sealed interface MainViewAction {
    data object LoadLastParking : MainViewAction
    data object TakePicture : MainViewAction
    data object AddNewParking : MainViewAction
    data object SaveCurrentLocation : MainViewAction
    data class UpdateNotes(val notes: String) : MainViewAction
    data class UpdateAddress(val address: String) : MainViewAction
}
