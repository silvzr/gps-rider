package com.dvhamham.manager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Displays an error dialog when the Xposed module is not active.
 *
 * @param onDismiss Callback to be invoked when the user dismisses the dialog.
 * @param onConfirm Callback to be invoked when the user confirms the dialog.
 */
@Composable
fun ErrorScreen(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Module Not Active") },
        text = {
            Text("GPS Rider module is not active in your Xposed manager app. Please enable it and restart the app to continue.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 