package com.uriolus.lastparking.presentation.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale

object DateMapper {

    fun formatTimestampToReadableDate(timestamp: Long): String {
        val date = Date(timestamp)
        // Using default locale's date and time format for broad compatibility.
        // You can customize this further e.g., DateFormat.MEDIUM, DateFormat.LONG
        // or SimpleDateFormat for a specific pattern.
        val dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.DEFAULT, 
            DateFormat.DEFAULT, 
            Locale.getDefault()
        )
        return dateFormat.format(date)
    }
}
