package com.uriolus.lastparking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uriolus.lastparking.R
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.presentation.viewstate.MainUiState
import com.uriolus.lastparking.ui.theme.LastParkingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState = MainUiState.Empty,
    onSaveParking: (Parking) -> Unit = {},
    onLoadParking: () -> Unit = {},
    onAddLocation: () -> Unit = {}
) {
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is MainUiState.Success -> {
                address = uiState.parking.address ?: ""
                notes = uiState.parking.notes
            }
            is MainUiState.Error -> {
                // Handle error state if needed
            }
            else -> {
                // Reset fields for other states
                address = ""
                notes = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.menu)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column {

                FloatingActionButton(
                    onClick = onAddLocation,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_location)
                    )
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is MainUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MainUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Map and Image Column - Takes 70% of screen height
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Map Placeholder
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState is MainUiState.Success) {
                                // Show map with location if available
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.map_location),
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                            } else {
                                Text("No location saved")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Image Placeholder
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState is MainUiState.Success && !uiState.parking.imageUri.isNullOrEmpty()) {
                                // Show saved image if available
                                // TODO: Load image from URI

                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_camera),
                                    contentDescription = stringResource(R.string.take_photo),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                    
                    // Form fields - Takes remaining 30% of screen
                    Column(
                        modifier = Modifier
                            .weight(0.3f)
                            .padding(horizontal = 16.dp)
                    ) {
                        // Address Field
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(stringResource(R.string.address)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            readOnly = uiState is MainUiState.Success
                        )
                        
                        // Notes Field
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(stringResource(R.string.comment)) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            readOnly = uiState is MainUiState.Success
                        )
                        
                        if (uiState is MainUiState.Success) {
                            Button(
                                onClick = { 
                                    onSaveParking(
                                        uiState.parking.copy(
                                            address = address,
                                            notes = notes
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Success(
                Parking(
                    id = "1",
                    notes = "Test comment",
                    latitude = 0.0,
                    longitude = 0.0,
                    address = "123 Main St"
                )
            ),
            onSaveParking = {},
            onLoadParking = {},
            onAddLocation = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenLoadingPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Loading,
            onSaveParking = {},
            onLoadParking = {},
            onAddLocation = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenErrorPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Error(AppError.ErrorLoading("Failed to load parking")),
            onSaveParking = {},
            onLoadParking = {},
            onAddLocation = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenEmptyPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Empty,
            onSaveParking = {},
            onLoadParking = {},
            onAddLocation = {}
        )
    }
}
