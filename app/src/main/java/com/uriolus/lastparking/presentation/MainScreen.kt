package com.uriolus.lastparking.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.uriolus.lastparking.R
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.presentation.viewmodel.MainUiState
import com.uriolus.lastparking.presentation.viewmodel.MainViewAction
import com.uriolus.lastparking.ui.theme.LastParkingTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper function to create an image URI
fun createImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "images")
    if (!imageDir.exists()) imageDir.mkdirs()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File(imageDir, "JPEG_${timeStamp}_.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", // Authority must match AndroidManifest
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    onAction: (MainViewAction) -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            val fineLocationGranted = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                onAction(MainViewAction.LoadLastParking) // Permissions granted, proceed
            } else {
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                onAction(MainViewAction.LocationPermissionDenied(shouldShowRationale))
            }
        }
    )

    // Effect to request permissions when UI state is RequestingPermission
    LaunchedEffect(uiState) {
        if (uiState is MainUiState.RequestingPermission) {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState) {
                            is MainUiState.NewParking -> stringResource(R.string.title_add_new_parking)
                            else -> stringResource(id = R.string.app_name)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (uiState is MainUiState.NewParking) {
                        IconButton(onClick = { onAction(MainViewAction.CancelAddNewParking) }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back)
                            )
                        }
                    }
                },
                actions = {
                    // TODO: Add any top bar actions if needed, like settings
                    if (uiState !is MainUiState.NewParking) {
                        IconButton(onClick = { /* TODO: Handle more options */ }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            when (val currentUiState = uiState) {
                is MainUiState.NewParking -> {
                    FloatingActionButton(
                        onClick = { onAction(MainViewAction.SaveCurrentLocation) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = stringResource(R.string.content_description_save_location)
                        )
                    }
                }
                is MainUiState.Success -> {
                    if (currentUiState.fabState.saveParking) {
                        FloatingActionButton(
                            onClick = { onAction(MainViewAction.SaveCurrentLocation) },
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Done, // Or LocationOn if preferred for save
                                contentDescription = stringResource(R.string.content_description_save_location)
                            )
                        }
                    } else if (currentUiState.fabState.newParking) {
                        // This is the FAB that was previously in ParkingScreen
                        FloatingActionButton(
                            onClick = {
                                val allPermissionsAlreadyGranted = locationPermissions.all {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        it
                                    ) == PackageManager.PERMISSION_GRANTED
                                }
                                if (allPermissionsAlreadyGranted) {
                                    onAction(MainViewAction.AddNewParkingClicked)
                                } else {
                                    locationPermissionLauncher.launch(locationPermissions)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_parking)
                            )
                        }
                    }
                }
                else -> { /* No FAB for Loading, Error, RequestingPermission states */ }
            }
        }
    ) { padding ->
        // Handle permission dialog states
        when (uiState) {
            is MainUiState.ShowLocationPermissionRationale -> {
                AlertDialog(
                    onDismissRequest = { onAction(MainViewAction.DismissPermissionDialogs) },
                    title = { Text(stringResource(R.string.permission_location_rationale_title)) },
                    text = { Text(stringResource(R.string.permission_location_rationale_message)) },
                    confirmButton = {
                        TextButton(onClick = { onAction(MainViewAction.RequestLocationPermissionAgain) }) {
                            Text(stringResource(R.string.permission_button_grant))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onAction(MainViewAction.DismissPermissionDialogs) }) {
                            Text(stringResource(R.string.permission_button_cancel))
                        }
                    }
                )
            }
            is MainUiState.ShowLocationPermissionPermanentlyDenied -> {
                AlertDialog(
                    onDismissRequest = { onAction(MainViewAction.DismissPermissionDialogs) },
                    title = { Text(stringResource(R.string.permission_location_denied_title)) },
                    text = { Text(stringResource(R.string.permission_location_denied_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            onAction(MainViewAction.DismissPermissionDialogs)
                        }) {
                            Text(stringResource(R.string.dialog_button_open_settings))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onAction(MainViewAction.DismissPermissionDialogs) }) {
                            Text(stringResource(R.string.permission_button_no_thanks))
                        }
                    }
                )
            }
            else -> { /* No dialog for other states */ }
        }

        when (uiState) {
            is MainUiState.Loading -> LoadingScreen(padding)
            is MainUiState.Error -> ErrorScreen(uiState, padding)
            is MainUiState.RequestingPermission -> RequestingPermissionScreen(padding) // Shows a simple text
            is MainUiState.PermissionRequiredButNotGranted -> PermissionDeniedPermanentlyScreen(padding) {
                onAction(MainViewAction.RequestLocationPermissionAgain)
            }
            is MainUiState.Success -> ParkingScreen(
                modifier = Modifier.padding(padding),
                parking = uiState.parking,
                hasChanges = uiState.fabState.saveParking,
                notModifiable = !uiState.fabState.saveParking,
                onAction = onAction
            )
            is MainUiState.NewParking -> NewParkingCaptureScreen(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onAction = onAction
            )

            MainUiState.ShowLocationPermissionPermanentlyDenied -> TODO()
            MainUiState.ShowLocationPermissionRationale -> TODO()
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
private fun RequestingPermissionScreen(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.requesting_permission))
    }
}

@Composable
private fun PermissionDeniedPermanentlyScreen(padding: PaddingValues, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.permission_denied_message_location),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.permission_button_retry))
        }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Map and Image section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f), // Changed weight to 0.7f
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Map display (first item)
            val mapLatLng = parking.location.let { LatLng(it.latitude, it.longitude) }
            val cameraPositionState = rememberCameraPositionState {
                mapLatLng.let { position = CameraPosition.fromLatLngZoom(it, 15f) }
            }
            LaunchedEffect(mapLatLng) {
                mapLatLng.let {
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f)
                    )
                }
            }

            GoogleMap(
                modifier = Modifier
                    .weight(1f) // Takes vertical space in this Column
                    .fillMaxWidth() // Fills width
                    .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)),
                cameraPositionState = cameraPositionState,
                uiSettings = com.google.maps.android.compose.MapUiSettings(
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    scrollGesturesEnabledDuringRotateOrZoom = false,
                    tiltGesturesEnabled = false
                )
            ) {
                Marker(
                    state = MarkerState(position = mapLatLng),
                    title = parking.address ?: stringResource(R.string.current_location_marker_title)
                )
            }

            // Image display (second item)
            if (!parking.imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = parking.imageUri,
                    contentDescription = stringResource(R.string.content_description_parking_image),
                    modifier = Modifier
                        .weight(1f) // Takes vertical space in this Column
                        .fillMaxWidth() // Fills width
                        .background(Color.DarkGray, shape = RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_camera) // Placeholder on error
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f) // Takes vertical space in this Column
                        .fillMaxWidth() // Fills width
                        .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = stringResource(R.string.content_description_parking_image),
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Editable fields (Address and Notes)
        EditableFields(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f), // Changed weight to 0.3f
            parking = parking,
            notModifiable = notModifiable,
            onAction = onAction
        )
    }
}

@Composable
fun EditableFields(
    modifier: Modifier = Modifier, // Added modifier parameter
    parking: Parking,
    notModifiable: Boolean,
    onAction: (MainViewAction) -> Unit
) {
    Column(
        modifier = modifier
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
            OutlinedTextField(
                value = parking.address ?: "",
                onValueChange = { newAddress ->
                    onAction(MainViewAction.UpdateAddress(newAddress))
                },
                label = { Text(stringResource(R.string.label_address)) },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
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

@Composable
fun NewParkingCaptureScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState.NewParking,
    onAction: (MainViewAction) -> Unit
) {
    val context = LocalContext.current
    var tempImageUri: Uri? by remember { mutableStateOf(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                onAction(MainViewAction.ImagePathUpdated(tempImageUri?.toString()))
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GpsAccuracyIndicator(accuracy = uiState.gpsAccuracy)

        val currentLocation = uiState.parking.location
        val mapLatLng = currentLocation?.let { LatLng(it.latitude, it.longitude) }

        val cameraPositionState = rememberCameraPositionState {
            mapLatLng?.let {
                position = CameraPosition.fromLatLngZoom(it, 15f) // Default zoom level
            }
        }

        // Update camera position when location changes
        LaunchedEffect(mapLatLng) {
            mapLatLng?.let {
                cameraPositionState.animate(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f)
                )
            }
        }

        if (mapLatLng != null) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = mapLatLng),
                    title = stringResource(R.string.current_location_marker_title)
                )
            }
        } else {
            // Placeholder if location is not yet available
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.map_waiting_for_location))
            }
        }

        // Image capture section
        if (uiState.parking.imageUri != null) {
            AsyncImage(
                model = uiState.parking.imageUri,
                contentDescription = stringResource(R.string.captured_image_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.DarkGray, shape = RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Button(
                onClick = {
                    tempImageUri = createImageUri(context)
                    cameraLauncher.launch(tempImageUri!!)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.take_picture_button_label))
            }
        }

        EditableFields(
            parking = uiState.parking,
            notModifiable = false,
            onAction = onAction
        )
    }
}

@Composable
fun GpsAccuracyIndicator(accuracy: Float?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val progress = when {
            accuracy == null -> 0.0f // Or use an indeterminate indicator
            accuracy <= 5f -> 1.0f
            accuracy <= 10f -> 0.75f
            accuracy <= 20f -> 0.5f
            else -> 0.25f
        }
        if (accuracy == null) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
        } else {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(64.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (accuracy != null) stringResource(R.string.label_gps_accuracy, accuracy)
            else stringResource(R.string.label_gps_accuracy_unavailable),
            style = MaterialTheme.typography.bodyMedium
        )
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


@Preview(showBackground = true)
@Composable
fun ParkingScreenPreviewNotModifiable() {
    LastParkingTheme {
        ParkingScreen(
            parking = Parking(
                id = "1",
                date = Date().toString(),
                location = ParkingLocation(0.0, 0.0),
                address = "123 Main St, Anytown, USA",
                notes = "Near the big oak tree.",
                imageUri = null
            ),
            hasChanges = false,
            notModifiable = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ParkingScreenPreviewModifiable() {
    LastParkingTheme {
        ParkingScreen(
            parking = Parking(
                id = "1",
                date = Date().toString(),
                location = ParkingLocation(0.0, 0.0),
                address = "123 Main St, Anytown, USA",
                notes = "Near the big oak tree.",
                imageUri = null
            ),
            hasChanges = true,
            notModifiable = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ParkingScreenPreviewEmpty() {
    LastParkingTheme {
        ParkingScreen(
            parking = EmptyParking,
            hasChanges = false,
            notModifiable = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewParkingCaptureScreenPreview() {
    LastParkingTheme {
        NewParkingCaptureScreen(
            uiState = MainUiState.NewParking(
                parking = EmptyParking.copy(notes = "Some notes here"),
                gpsAccuracy = 12.5f
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GpsAccuracyIndicatorPreview() {
    LastParkingTheme {
        Column {
            GpsAccuracyIndicator(accuracy = 3.0f)
            Spacer(Modifier.height(16.dp))
            GpsAccuracyIndicator(accuracy = 8.0f)
            Spacer(Modifier.height(16.dp))
            GpsAccuracyIndicator(accuracy = 15.0f)
            Spacer(Modifier.height(16.dp))
            GpsAccuracyIndicator(accuracy = 25.0f)
            Spacer(Modifier.height(16.dp))
            GpsAccuracyIndicator(accuracy = null)
        }
    }
}
