package com.uriolus.lastparking.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.uriolus.lastparking.presentation.viewstate.MainUiState
import com.uriolus.lastparking.ui.theme.LastParkingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState: MainUiState by viewModel.uiState.collectAsState()
            LastParkingTheme {
                MainScreen(
                    uiState = uiState,
                    onSaveParking = viewModel::saveParking,
                    onLoadParking = viewModel::loadLastParking
                )
            }
        }
    }
}