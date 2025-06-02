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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uriolus.lastparking.R
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.presentation.contract.MainAction
import com.uriolus.lastparking.presentation.viewstate.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    onAction: (MainAction) -> Unit,
) {
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
                    onClick = { onAction(MainAction.AddNewLocation) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_location)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { onAction(MainAction.TakePicture) },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = "Take picture"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { onAction(MainAction.SaveCurrentLocation) },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Save current location"
                    )
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is MainUiState.Empty -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) { EmptyScreen() }

            is MainUiState.Loading -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) { LoadingScreen() }

            is MainUiState.Error -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) { ErrorScreen(uiState) }

            is MainUiState.Success -> SuccessScreen(
                modifier = modifier.padding(padding),
                parking = uiState.parking
            )
        }
    }
}


@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyScreen() {
    SuccessScreen(
        modifier = Modifier.fillMaxSize(),
        parking = Parking(
            id = "0",
            notes = "",
            imageUri = null,
            latitude = 0.0,
            longitude = 0.0
        )
    )
}

@Composable
private fun ErrorScreen(error: MainUiState.Error) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: ${error.error}",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SuccessScreen(
    modifier: Modifier = Modifier,
    parking: Parking
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
                // Show map with location if available
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.map_location),
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
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
                // Show saved image if available
                if (parking.imageUri.isNullOrEmpty()) {
                    // TODO: Load image from URI
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = "Parking image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


