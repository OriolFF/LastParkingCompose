package com.uriolus.lastparking.presentation

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.uriolus.lastparking.R
import com.uriolus.lastparking.presentation.viewmodel.MainViewAction

@Composable
internal fun HandleLocationPermissionRequest(
    onAction: (MainViewAction) -> Unit
) {
    var showDialog by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    RequestingPermission(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionGranted = {
            onAction(MainViewAction.LocationPermissionGranted)
        },
        onPermissionDenied = { requestPermissionAgain ->
            showDialog = {
                PermissionRationaleOrDeniedDialog(
                    title = stringResource(id = R.string.permission_location_denied_title),
                    message = stringResource(id = R.string.permission_location_denied_message),
                    confirmButtonText = stringResource(id = R.string.permission_button_retry),
                    onConfirm = {
                        showDialog = null
                        requestPermissionAgain()
                    },
                    dismissButtonText = stringResource(id = R.string.permission_button_cancel),
                    onDismiss = {
                        showDialog = null
                        onAction(MainViewAction.LocationPermissionRequestCancelled)
                    }
                )
            }
        },
        onPermissionRationaleNeeded = { requestPermission ->
            showDialog = {
                PermissionRationaleOrDeniedDialog(
                    title = stringResource(id = R.string.permission_location_rationale_title),
                    message = stringResource(id = R.string.permission_location_rationale_message),
                    confirmButtonText = stringResource(id = R.string.permission_button_grant),
                    onConfirm = {
                        showDialog = null
                        requestPermission()
                    },
                    dismissButtonText = stringResource(id = R.string.permission_button_no_thanks),
                    onDismiss = {
                        showDialog = null
                        onAction(MainViewAction.LocationPermissionRequestCancelled)
                    }
                )
            }
        }
    )
    // Show the dialog if it's set
    showDialog?.invoke()
}

@Composable
private fun PermissionRationaleOrDeniedDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        }
    )
}
