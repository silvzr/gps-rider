package com.dvhamham.manager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dvhamham.R

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
        title = { Text(stringResource(R.string.module_not_active_dialog_title)) },
        text = {
            Text(stringResource(R.string.module_not_active_dialog_message))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 