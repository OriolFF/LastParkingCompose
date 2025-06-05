package com.uriolus.lastparking.presentation

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Helper enum for permission status
internal enum class PermissionUiStatus {
    GRANTED,
    DENIED, // Permission not granted, rationale not required or already shown and denied again
    RATIONALE_REQUIRED // Permission not granted, rationale should be shown
}

/**
 * A composable that manages the UI flow for requesting a single runtime permission.
 *
 * @param permission The Android permission string to request (e.g., `Manifest.permission.ACCESS_FINE_LOCATION`).
 * @param onPermissionGranted Composable content to display when the permission is granted.
 * @param onPermissionDenied Composable content to display when the permission is denied and no rationale is currently required.
 *                           Provides a lambda `requestPermission` to trigger the permission request flow again.
 * @param onPermissionRationaleNeeded Composable content to display when the system indicates that a rationale should be shown to the user before requesting again.
 *                                    Provides a lambda `requestPermission` to trigger the permission request flow again.
 */
@Composable
fun RequestingPermission(
    permission: String,
    onPermissionGranted: @Composable () -> Unit,
    onPermissionDenied: @Composable (requestPermission: () -> Unit) -> Unit,
    onPermissionRationaleNeeded: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var permissionStatus by remember {
        mutableStateOf(getPermissionStatus(context, permission))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = if (isGranted) {
            PermissionUiStatus.GRANTED
        } else {
            // If denied, check if rationale is now needed for a future request.
            if (shouldShowRationale(context, permission)) {
                PermissionUiStatus.RATIONALE_REQUIRED
            } else {
                PermissionUiStatus.DENIED
            }
        }
    }

    // Effect to update status if it changes externally (e.g., granted in settings)
    // This runs when the composable enters the composition or if context/permission changes.
    LaunchedEffect(key1 = context, key2 = permission) {
        val currentStatus = getPermissionStatus(context, permission)
        if (permissionStatus != currentStatus) {
             permissionStatus = currentStatus
        }
    }

    when (permissionStatus) {
        PermissionUiStatus.GRANTED -> {
            onPermissionGranted()
        }
        PermissionUiStatus.DENIED -> {
            onPermissionDenied { launcher.launch(permission) }
        }
        PermissionUiStatus.RATIONALE_REQUIRED -> {
            onPermissionRationaleNeeded { launcher.launch(permission) }
        }
    }
}

private fun getPermissionStatus(context: Context, permission: String): PermissionUiStatus {
    return when {
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
            PermissionUiStatus.GRANTED
        }
        shouldShowRationale(context, permission) -> {
            PermissionUiStatus.RATIONALE_REQUIRED
        }
        else -> {
            PermissionUiStatus.DENIED
        }
    }
}

// It's important to use an Activity context for shouldShowRequestPermissionRationale
private fun shouldShowRationale(context: Context, permission: String): Boolean {
    val activity = context as? Activity
    // If context is not an Activity, we cannot determine if rationale should be shown.
    // In such rare cases (e.g., preview), default to false.
    return activity?.shouldShowRequestPermissionRationale(permission) ?: false
}
