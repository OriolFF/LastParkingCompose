package com.uriolus.lastparking.domain.model

sealed class AppError {
        data class ErrorSaving(val errorMsg: String) : AppError()
        data class ErrorLoading(val errorMsg: String) : AppError()
            data object ErrorNoPreviousParking : AppError()
    data object LocationPermissionDenied : AppError()
}