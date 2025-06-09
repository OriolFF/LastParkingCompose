package com.uriolus.lastparking.domain.model

sealed class AppError {
    data class ErrorSaving(val errorMsg: String) : AppError()
    data class ErrorLoading(val errorMsg: String) : AppError()
    data object ErrorTakingPicture: AppError()
    data object ErrorNoPreviousParking : AppError()
    data object LocationPermissionDenied : AppError()
    data object GeocoderNotAvailable : AppError()
    data object NoAddressFound : AppError()
    data class GeocodingIOError(val errorMsg: String) : AppError()
    data class GeocodingIllegalArgument(val errorMsg: String) : AppError()
}