package com.uriolus.lastparking.domain.model

sealed class AppError(msg: String) {
    data class ErrorSaving(val errorMsg: String) : AppError(msg = errorMsg)
    data class ErrorLoading(val errorMsg: String) : AppError(msg = errorMsg)
}