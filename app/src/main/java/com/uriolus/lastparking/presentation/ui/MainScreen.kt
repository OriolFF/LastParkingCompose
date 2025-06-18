package com.uriolus.lastparking.presentation.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.uriolus.lastparking.R
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.presentation.util.DateMapper
import com.uriolus.lastparking.presentation.viewmodel.MainUiState
import com.uriolus.lastparking.presentation.viewmodel.MainViewAction
import com.uriolus.lastparking.presentation.viewmodel.MainViewEvent
import com.uriolus.lastparking.ui.theme.LastParkingTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

// Helper function to create an image URI
fun createImageUri(context: Context): Uri {
    val imageFileName = "last_parking_image.jpg"
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(storageDir, imageFileName)
    return FileProvider.getUriForFile(
        context,
        "com.uriolus.lastparking.fileprovider",
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    events: Flow<MainViewEvent>,
    onAction: (MainViewAction) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val activity: Activity? = LocalActivity.current

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Camera Launcher using TakePicture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean -> // Changed to non-nullable Boolean
            onAction(MainViewAction.CameraResult(success)) // Simplified: success is true or false
        }
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            val fineLocationGranted =
                permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted =
                permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                onAction(MainViewAction.LocationPermissionGranted)
            } else {
                val shouldShowRationale = activity?.let { act -> 
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        act,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } ?: false // Default to false if activity is null
                onAction(MainViewAction.LocationPermissionDenied(shouldShowRationale))
            }
        }
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is MainUiState.RequestingPermission -> {
                locationPermissionLauncher.launch(locationPermissions)
            }
            is MainUiState.NewParking -> {
                val newImageUri = createImageUri(context)
                onAction(MainViewAction.NewParkingScreenStarted(newImageUri.toString()))
            }
            else -> {
                // No side effect for other states in this LaunchedEffect
            }
        }
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            when (event) {
                is MainViewEvent.TakeAPicture -> {
                    val currentImageUriForSaving = event.uriImage.toUri()
                    cameraLauncher.launch(currentImageUriForSaving)
                }

                is MainViewEvent.ShowMessage -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = event.message)
                    }
                }

                is MainViewEvent.ShowError -> {
                    Log.e("MainScreen", "Event: ShowError - ${event.error}")
                }

                is MainViewEvent.OnWalkToLocation -> {
                    val gmmIntentUri = "google.navigation:q=${event.location.latitude},${event.location.longitude}&mode=w".toUri()
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    if (uiState is MainUiState.NewParking && !uiState.isInitialFlow) {
                        IconButton(onClick = { onAction(MainViewAction.CancelAddNewParking) }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back)
                            )
                        }
                    }
                },
                actions = {
                    if (uiState is MainUiState.Success) {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    onAction(MainViewAction.DeleteCurrentParking)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
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
            when (uiState) {
                is MainUiState.NewParking -> {
                    FloatingActionButton(
                        onClick = { onAction(MainViewAction.SaveCurrentLocation) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer // Changed color
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.content_description_save_location)
                        )
                    }
                }

                is MainUiState.Success -> {
                    if (uiState.fabState.saveParking) {
                        FloatingActionButton(
                            onClick = { onAction(MainViewAction.SaveCurrentLocation) },
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Done, // Or LocationOn if preferred for save
                                contentDescription = stringResource(R.string.content_description_save_location)
                            )
                        }
                    } else if (uiState.fabState.newParking) {
                        FloatingActionButton(
                            onClick = {
                                val allPermissionsAlreadyGranted = locationPermissions.all {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        it
                                    ) == PackageManager.PERMISSION_GRANTED
                                }
                                if (allPermissionsAlreadyGranted) {
                                    onAction(MainViewAction.StartNewParkingFlow)
                                } else {
                                    Log.d("MainScreen", "Permissions not granted. Launching location permission request.")
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

                else -> { /* No FAB for Loading, Error, RequestingPermission states */
                }
            }
        }
    ) { paddingValues ->
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
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
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

            is MainUiState.Loading -> LoadingScreen(paddingValues)
            is MainUiState.Error -> ErrorScreen(uiState, paddingValues)
            is MainUiState.RequestingPermission -> RequestingPermissionScreen(paddingValues) // Shows a simple text
            is MainUiState.PermissionRequiredButNotGranted -> PermissionDeniedPermanentlyScreen(
                paddingValues
            ) {
                onAction(MainViewAction.RequestLocationPermissionAgain)
            }

            is MainUiState.Success -> ParkingScreen(
                modifier = Modifier.padding(paddingValues),
                parking = uiState.parking,
                notModifiable = true,
                onAction = onAction
            )

            is MainUiState.NewParking -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                GpsAccuracyIndicator(accuracy = uiState.gpsAccuracy)
                Spacer(modifier = Modifier.height(8.dp))
                ParkingScreen(
                    modifier = Modifier.weight(1f),
                    parking = uiState.parking,
                    notModifiable = false,
                    onAction = onAction
                )
            }

            is MainUiState.InitialNewParkingRequiresPermissionCheck -> {
                // This state means the app has loaded, found no prior parking, and needs to check permissions
                // before proceeding to the new parking flow (which includes location updates).
                val allPermissionsGranted = locationPermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                if (allPermissionsGranted) {
                    // Permissions are already granted, proceed to get location for new parking
                    LaunchedEffect(Unit) { // Use LaunchedEffect to call onAction once
                        onAction(MainViewAction.ProceedWithInitialNewParking)
                    }
                } else {
                    // Permissions are not granted, request them.
                    // The existing locationPermissionLauncher will handle the result.
                    LaunchedEffect(Unit) { // Use LaunchedEffect to launch once
                        Log.d("MainScreen", "InitialNewParkingRequiresPermissionCheck: Permissions not granted. Launching request.")
                        locationPermissionLauncher.launch(locationPermissions)
                    }
                }
                // Optionally, show a loading or specific UI for this check, 
                // but for now, it will be a quick check and transition.
                // A simple Text can be shown if the check/launch takes noticeable time.
                RequestingPermissionScreen(paddingValues) // Re-use for visual consistency during check/launch
            }
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

@Preview(showBackground = true, name = "Main Screen Preview")
@Composable
fun MainScreenPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Success(
                parking = Parking(
                    id = "1",
                    notes = stringResource(R.string.preview_notes_provisional),
                    location = ParkingLocation(0.0, 0.0, 0f),
                    address = stringResource(R.string.preview_address_provisional),
                    date = DateMapper.formatTimestampToReadableDate(System.currentTimeMillis()),
                    imageUri = null,
                    timestamp = System.currentTimeMillis(),
                    mapUri = null
                )
            ),
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "GpsAccuracyIndicator Preview")
@Composable
fun GpsAccuracyIndicatorPreview() {
    LastParkingTheme {
        Column {
            GpsAccuracyIndicator(accuracy = 5f)
            GpsAccuracyIndicator(accuracy = 15f)
            GpsAccuracyIndicator(accuracy = 25f)
            GpsAccuracyIndicator(accuracy = null)
        }
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun MainScreenLoadingPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Loading,
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Requesting Permission State")
@Composable
fun MainScreenRequestingPermissionPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.RequestingPermission,
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Show Permission Rationale State")
@Composable
fun MainScreenShowPermissionRationalePreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.ShowLocationPermissionRationale,
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "New Parking State - Initial Flow")
@Composable
fun MainScreenNewParkingInitialFlowPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.NewParking(
                parking = EmptyParking.copy(timestamp = System.currentTimeMillis()),
                gpsAccuracy = 10f,
                isInitialFlow = true
            ),
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "New Parking State - User Initiated")
@Composable
fun MainScreenNewParkingUserInitiatedPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.NewParking(
                parking = EmptyParking.copy(timestamp = System.currentTimeMillis()),
                gpsAccuracy = 5f,
                isInitialFlow = false
            ),
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun MainScreenErrorPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Error(AppError.ErrorSaving("Preview error message")),
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Success State - No Location")
@Composable
fun MainScreenSuccessNoLocationPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Success(
                parking = Parking(
                    id = "1",
                    notes = "Near the big oak tree, no location.",
                    location = null,
                    address = "Address not available",
                    date = DateMapper.formatTimestampToReadableDate(System.currentTimeMillis()),
                    imageUri = null,
                    timestamp = System.currentTimeMillis(),
                    mapUri = null
                )
            ),
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Success State - With Location")
@Composable
fun MainScreenSuccessWithLocationPreview() {
    LastParkingTheme {
        MainScreen(
            uiState = MainUiState.Success(
                parking = Parking(
                    id = "1",
                    notes = "By the fountain.",
                    location = ParkingLocation(40.7128, -74.0060),
                    address = "1 Park Row, New York, NY",
                    date = DateMapper.formatTimestampToReadableDate(System.currentTimeMillis()),
                    imageUri = null, // Replace with a sample image URI for full preview
                    timestamp = System.currentTimeMillis(),
                    mapUri = null // Replace with a sample map URI string for full preview
                )
            ),
            events = MutableSharedFlow(),
            onAction = {}
        )
    }
}

// Helper function to save Bitmap to a Uri
private fun saveBitmapToUri(context: Context, bitmap: Bitmap, uri: Uri): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        true
    } catch (e: Exception) {
        Log.e("MainScreen", "Error saving bitmap to URI: $uri", e)
        false
    }
}
