package com.uriolus.lastparking.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.uriolus.lastparking.R
import com.uriolus.lastparking.domain.model.EmptyParking
import com.uriolus.lastparking.domain.model.Parking
import com.uriolus.lastparking.presentation.util.DateMapper
import com.uriolus.lastparking.presentation.viewmodel.MainViewAction
import com.uriolus.lastparking.ui.theme.LastParkingTheme

@Composable
fun ParkingScreen(
    modifier: Modifier = Modifier,
    parking: Parking,
    notModifiable: Boolean = false,
    onAction: (MainViewAction) -> Unit = {}
) {

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp) // Apply horizontal padding once
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Map image takes 30% of the available height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
        ) {
            MapImage(
                mapUri = parking.mapUri,
                modifier = Modifier.fillMaxSize()
            )
            if (parking.location != null && notModifiable) {
                IconButton(
                    onClick = { onAction(MainViewAction.WalkToLocation) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = stringResource(R.string.content_description_walk_to_location),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Picture image takes 40% of the available height
        PictureImage(
            imageUri = parking.imageUri,
            onAction = onAction,
            isActionable = !notModifiable,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display timestamp if it's an existing parking

        if (parking.timestamp > 0L) {
            val formattedDate =
                DateMapper.formatTimestampToReadableDate(parking.timestamp)
            // Assuming R.string.label_parked_at is "Parked at: %1$s"
            val fullText = stringResource(R.string.label_parked_at, formattedDate)

            Text(
                text = fullText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Start) // Align text to the start
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Log.d(
                "ParkingScreen",
                "Timestamp is not > 0L, not displaying. Timestamp: ${parking.timestamp}"
            )
        }

        val rememberedNotModifiable by remember(notModifiable) { derivedStateOf { notModifiable } }

        // Editable fields at the bottom
        EditableFields(
            parking = parking,
            notModifiable = rememberedNotModifiable,
            onAction = onAction
        )

        // Spacer at the bottom to avoid overlapping with a FAB.
        // A standard FAB is 56dp tall + 16dp padding = 72dp.
        Spacer(modifier = Modifier.height(72.dp))
    }
}


@Composable
fun PictureImage(
    imageUri: String?,
    onAction: (MainViewAction) -> Unit,
    isActionable: Boolean,
    modifier: Modifier = Modifier // Accept modifier from the parent
) {
    val context = LocalContext.current
    if (!imageUri.isNullOrEmpty()) {
        val imageRequest = ImageRequest.Builder(context)
            .data(imageUri)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build()
        AsyncImage(
            model = imageRequest,
            contentDescription = stringResource(R.string.content_description_parking_image),
            modifier = modifier // Use the modifier passed from the parent
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    Modifier.clickable { onAction(MainViewAction.ImageClicked) }
                ),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(id = R.drawable.ic_map_placeholder),
            error = painterResource(id = R.drawable.ic_map_placeholder),
            onError = { errorResult ->
                Log.e("PictureImage", "Error loading image: ${errorResult.result.throwable}")
            }
        )
    } else {
        NoImagePlaceholder(
            onAction = onAction,
            isActionable = isActionable,
            modifier = modifier
        )
    }
}


@Composable
fun MapImage(mapUri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if (!mapUri.isNullOrEmpty()) {
        val imageRequest = ImageRequest.Builder(context)
            .data(mapUri)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build()
        AsyncImage(
            model = imageRequest,
            contentDescription = stringResource(R.string.content_description_map_image),
            modifier = modifier // Use the modifier from the parent
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_map_placeholder),
            error = painterResource(id = R.drawable.ic_map_placeholder),
            onError = { errorResult ->
                Log.w(
                    "MapImage",
                    "Error loading mapUri: $mapUri. Exception: ${errorResult.result.throwable}"
                )
            }
        )
    } else {
        NoMapPlaceholder(modifier = modifier) // Pass the modifier
    }
}


@Composable
fun NoImagePlaceholder(
    onAction: (MainViewAction) -> Unit,
    isActionable: Boolean,
    modifier: Modifier = Modifier // Accept modifier
) {
    // The Box is necessary here to center the Icon inside the placeholder area.
    Box(
        modifier = modifier // Use the passed modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (isActionable) Modifier.clickable { onAction(MainViewAction.ImageClicked) }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isActionable) {
            Icon(
                imageVector = Icons.Filled.Camera,
                contentDescription = stringResource(R.string.content_description_take_picture_placeholder),
                modifier = Modifier.size(64.dp), // Use size for square icons
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = stringResource(R.string.content_description_no_image_available),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun NoMapPlaceholder(modifier: Modifier = Modifier) {
    // Box is necessary to center the Text.
    Box(
        modifier = modifier
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

    // This Column is fine as it groups the Text/TextFields together.
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Adds space between fields
    ) {

            OutlinedTextField(
                value = parking.address?:"",
                onValueChange = { newAddress ->
                    onAction(MainViewAction.UpdateAddress(newAddress))
                },
                label = { Text(stringResource(R.string.label_address)) },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium,
                readOnly = notModifiable
            )
            OutlinedTextField(
                value = parking.notes?:"",
                onValueChange = { newNotes ->
                    onAction(MainViewAction.UpdateNotes(newNotes))
                },
                label = { Text(stringResource(R.string.label_notes)) },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium,
                readOnly = notModifiable
            )
        }

}

@Preview(showBackground = true, name = "Parking Screen - Empty")
@Composable
fun ParkingScreenPreviewEmpty() {
    LastParkingTheme {
        ParkingScreen(
            parking = EmptyParking,
            notModifiable = false
        )
    }
}

@Preview(showBackground = true, name = "Parking Screen - With Data")
@Composable
fun ParkingScreenPreviewWithData() {
    LastParkingTheme {
        ParkingScreen(
            parking = Parking(
                id = "1",
                timestamp = System.currentTimeMillis(),
                location = null, // Or provide a mock ParkingLocation
                address = "123 Main St, Anytown, USA",
                notes = "Near the big oak tree. This is a rather long note to see how it wraps and if it fits well within the allocated space.",
                imageUri = null, // Provide a sample image URI if available for preview, or leave null
                mapUri = null // Provide a sample map URI if available for preview, or leave null
            ),
            notModifiable = false
        )
    }
}

@Preview(showBackground = true, name = "Parking Screen - Not Modifiable")
@Composable
fun ParkingScreenPreviewNotModifiable() {
    LastParkingTheme {
        ParkingScreen(
            parking = Parking(
                id = "2",
                timestamp = System.currentTimeMillis(),
                location = null,
                address = "456 Oak Ave, Otherville, USA",
                notes = "Confidential parking spot.",
                imageUri = null,
                mapUri = null
            ),
            notModifiable = true
        )
    }
}
