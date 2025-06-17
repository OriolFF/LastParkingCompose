package com.uriolus.lastparking.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.uriolus.lastparking.presentation.ui.MainScreen
import com.uriolus.lastparking.presentation.viewmodel.MainViewModel
import com.uriolus.lastparking.ui.theme.LastParkingTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LastParkingTheme {
                val uiState by viewModel.uiState.collectAsState()
                MainScreen(
                    uiState = uiState,
                    events = viewModel.events,
                    onAction = viewModel::handleAction
                )
            }
        }
    }
}