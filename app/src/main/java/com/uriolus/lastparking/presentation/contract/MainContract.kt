package com.uriolus.lastparking.presentation.contract

sealed interface MainEvent {
    data class ShowMessage(val message: String) : MainEvent
    data class NavigateTo(val route: String) : MainEvent
}

sealed interface MainAction {
    data object LoadLastParking : MainAction
    data object TakePicture : MainAction
    data object AddNewLocation : MainAction
    data object SaveCurrentLocation : MainAction
    data class UpdateNotes(val notes: String) : MainAction
    data class UpdateAddress(val address: String) : MainAction
}
