package com.uriolus.lastparking.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.uriolus.lastparking.ui.theme.LastParkingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LastParkingTheme {
                MainScreen(
                    onAddLocation = { /* TODO: Handle add location */ }
                )
            }
        }
    }
}