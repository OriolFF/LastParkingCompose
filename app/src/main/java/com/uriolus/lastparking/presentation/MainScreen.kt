package com.uriolus.lastparking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uriolus.lastparking.R
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.domain.repository.LocationRepository
import com.uriolus.lastparking.presentation.viewmodel.MainUiState
import com.uriolus.lastparking.presentation.viewmodel.MainViewAction
import com.uriolus.lastparking.ui.theme.LastParkingTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    onAction: (MainViewAction) -> Unit,
) {
    val locationRepository: LocationRepository = koinInject()

    LaunchedEffect(uiState) {
        if (uiState is MainUiState.NewParking) {
            locationRepository.getLocationUpdates().collect { location ->
                onAction(MainViewAction.UpdateLocation(location))
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
                ),
                windowInsets = WindowInsets(top = 0.dp)
            )
        },
        // No floatingActionButton here; FABs are handled in SuccessScreen
    ) { padding: PaddingValues ->
        when (uiState) {
            is MainUiState.Loading -> LoadingScreen(padding)

            is MainUiState.Error -> ErrorScreen(uiState, padding)

            is MainUiState.Success -> ParkingScreen(
                modifier = modifier.padding(padding),
                parking = uiState.parking,
                hasChanges = false,
                notModifiable = true,
                onAction = onAction
            )

            is MainUiState.NewParking -> ParkingScreen(
                modifier = modifier.padding(padding),
                parking = uiState.parking,
                hasChanges = false,
                notModifiable = false,
                onAction = onAction
            )

            MainUiState.RequestingPermission -> TODO()
        }
    }
}

@Composable
private fun LoadingScreen(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(error: MainUiState.Error, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.error_message_generic, error.error.toString()),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ParkingScreen(
    modifier: Modifier = Modifier,
    parking: Parking,
    hasChanges: Boolean = false,
    notModifiable: Boolean = false,
    onAction: (MainViewAction) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 0.dp, bottom = 0.dp),
            ) {
                // Map and Image section (70% of height)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .background(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.map_location),
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .background(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (parking.imageUri.isNullOrEmpty()) {
                            // TODO: Load image from URI
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = stringResource(R.string.content_description_parking_image),
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            // TODO: Load image from URI
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = stringResource(R.string.content_description_parking_image),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                // Address and Notes section (20% of height)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f),
                    verticalArrangement = Arrangement.Center
                ) {
                    ParkingScreen(
                        parking = parking,
                        notModifiable = notModifiable,
                        onAction = onAction
                    )
                }
                // Spacer for bottom 10%
                Spacer(modifier = Modifier.weight(0.1f))
            }
            // FAB logic
            if (parking != EmptyParking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (hasChanges) {
                        FloatingActionButton(
                            onClick = { onAction(MainViewAction.SaveCurrentLocation) },
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.content_description_save_location)
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { onAction(MainViewAction.AddNewParkingClicked) },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_parking)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParkingScreen(
    parking: Parking,
    notModifiable: Boolean,
    onAction: (MainViewAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        if (notModifiable) {
            Text(
                text = stringResource(R.string.label_address) + ": ${parking.address ?: stringResource(R.string.text_no_address_available)}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(text = stringResource(R.string.label_notes) + ": ${parking.notes}", style = MaterialTheme.typography.bodyLarge)
        } else {
            TextField(
                value = parking.address ?: "",
                onValueChange = { newAddress ->
                    onAction(MainViewAction.UpdateAddress(newAddress))
                },
                label = { Text(stringResource(R.string.label_address)) },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium
            )
            TextField(
                value = parking.notes,
                onValueChange = { newNotes ->
                    onAction(MainViewAction.UpdateNotes(newNotes))
                },
                label = { Text(stringResource(R.string.label_notes)) },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium
            )
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
                    id = "0",
                    notes = stringResource(R.string.preview_notes_provisional),
                    imageUri = null,
                    location = ParkingLocation(0.0, 0.0),
                    address = stringResource(R.string.preview_address_provisional)
                )
            ),
            onAction = {}
        )
    }
}
