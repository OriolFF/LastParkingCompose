package com.uriolus.lastparking.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BrokenImage
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import coil.request.ErrorResult
import com.uriolus.lastparking.R
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.domain.model.ParkingLocation
import com.uriolus.lastparking.presentation.ui.GpsAccuracyIndicator
import com.uriolus.lastparking.presentation.viewmodel.MainUiState
import com.uriolus.lastparking.presentation.viewmodel.MainViewAction
import com.uriolus.lastparking.presentation.viewmodel.MainViewEvent
import com.uriolus.lastparking.ui.theme.LastParkingTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

// Helper function to create an image URI
fun createImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "images")
    if (!imageDir.exists()) imageDir.mkdirs()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File(imageDir, "JPEG_${timeStamp}_.jpg")
    try {
        imageFile.createNewFile() // Ensure the file exists
    } catch (e: java.io.IOException) {
        Log.e("MainScreen", "Failed to create image file", e)
        // Handle error appropriately, perhaps return a fallback Uri or throw
    }
    return FileProvider.getUriForFile(
        context,
        "com.uriolus.lastparking.fileprovider", // Corrected authority
        imageFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    events: SharedFlow<MainViewEvent>,
    onAction: (MainViewAction) -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

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
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                onAction(MainViewAction.LocationPermissionDenied(shouldShowRationale))
            }
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is MainUiState.RequestingPermission) {
            locationPermissionLauncher.launch(locationPermissions)
        }
        if (uiState is MainUiState.NewParking) {
            val newImageUri = createImageUri(context)
            onAction(MainViewAction.NewParkingScreenStarted(newImageUri))
        }
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            when (event) {
                is MainViewEvent.TakeAPicture -> {
                    val currentImageUriForSaving = event.uriImage
                    cameraLauncher.launch(currentImageUriForSaving)
                }
                is MainViewEvent.ShowMessage -> {
                    Log.d("MainScreen", "Event: ShowMessage - ${event.message}")
                }
                is MainViewEvent.ShowError -> {
                    Log.e("MainScreen", "Event: ShowError - ${event.error}")
                }
                is MainViewEvent.NavigateTo -> {
                    Log.d("MainScreen", "Event: NavigateTo - ${event.route}")
                }
            }
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
            when (uiState) {
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
                notModifiable = !uiState.fabState.saveParking,
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
fun ParkingScreen(
    modifier: Modifier = Modifier,
    parking: Parking,
    hasChanges: Boolean = false,
    notModifiable: Boolean = false,
    onAction: (MainViewAction) -> Unit = {}
) {
    val rememberedParking by remember(parking.id, parking.location, parking.address, 
                                      parking.notes, parking.imageUri, parking.mapUri) {
        derivedStateOf { parking }
    }
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
       MapImage(rememberedParking.mapUri)
        
       PictureImage(rememberedParking.imageUri, onAction = onAction, isActionable = !notModifiable)

        Spacer(modifier = Modifier.height(8.dp))

        val rememberedNotModifiable by remember(notModifiable) { derivedStateOf { notModifiable } }
        
        EditableFields(
            parking = rememberedParking,
            notModifiable = rememberedNotModifiable,
            onAction = onAction
        )
    }
}

@Composable
fun PictureImage(imageUri: String?, onAction: (MainViewAction) -> Unit, isActionable: Boolean) {
    Log.d("PictureImage", "Attempting to load imageUri: $imageUri, isActionable: $isActionable")
    if (!imageUri.isNullOrEmpty()) {

        AsyncImage(
            model = imageUri,
            contentDescription = stringResource(R.string.content_description_parking_image),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant) // Placeholder background
                .then(
                    if (isActionable) {
                        Modifier.clickable { onAction(MainViewAction.TakePicture) }
                    } else {
                        Modifier
                    }
                ),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_map_placeholder), // Generic placeholder
            error = painterResource(id = R.drawable.ic_map_placeholder), // Generic error placeholder
            onError = { errorResult ->
                Log.e("PictureImage", "Error loading image: ${errorResult.result.throwable}")
            }
        )
    } else {
        Log.d("PictureImage", "imageUri is null or empty, showing placeholder.")
        NoImagePlaceholder(onAction = onAction, isActionable = isActionable)
    }
}

@Composable
fun MapImage(mapUri: String?) {
    if (!mapUri.isNullOrEmpty()) {
        Log.d("MapUri", "MapUri: $mapUri")
        AsyncImage(
            model = mapUri,
            contentDescription = stringResource(R.string.content_description_map_image),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant), // Placeholder background
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_map_placeholder), // Generic placeholder
            error = painterResource(id = R.drawable.ic_map_placeholder),
            onError = { errorResult ->
                Log.w("MapImage", "Error loading mapUri: $mapUri. Exception: ${errorResult.result.throwable}")
            }
        )
    } else {
        NoMapPlaceholder()
    }
}

@Composable
fun NoImagePlaceholder(onAction: (MainViewAction) -> Unit, isActionable: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (isActionable) Modifier.clickable { onAction(MainViewAction.TakePicture) }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isActionable) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = stringResource(R.string.content_description_take_picture_placeholder),
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = stringResource(R.string.content_description_no_image_available),
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun NoMapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.no_image_available))
    }
}

@Composable
fun EditableFields(
    modifier: Modifier = Modifier,
    parking: Parking,
    notModifiable: Boolean,
    onAction: (MainViewAction) -> Unit
) {
    val rememberedAddress by remember(parking.address) { derivedStateOf { parking.address ?: "" } }
    val rememberedNotes by remember(parking.notes) { derivedStateOf { parking.notes } }
    
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        if (notModifiable) {
            Text(
                text = stringResource(R.string.label_address) + ": ${
                    rememberedAddress.ifEmpty { stringResource(R.string.text_no_address_available) }
                }",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.label_notes) + ": ${rememberedNotes}",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            OutlinedTextField(
                value = rememberedAddress,
                onValueChange = { newAddress ->
                    onAction(MainViewAction.UpdateAddress(newAddress))
                },
                label = { Text(stringResource(R.string.label_address)) },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = rememberedNotes,
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
                    id = "1",
                    location = ParkingLocation(0.0, 0.0, 0f),
                    address = stringResource(R.string.preview_address_provisional),
                    notes = stringResource(R.string.preview_notes_provisional),
                    imageUri = null
                )
            ),
            events = MutableSharedFlow(),
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
