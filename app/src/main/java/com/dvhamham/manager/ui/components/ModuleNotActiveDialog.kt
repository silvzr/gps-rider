package com.dvhamham.manager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dvhamham.R

@Composable
fun ModuleNotActiveDialog() {
    AlertDialog(
        onDismissRequest = {}, // Non-cancelable
        title = { Text(stringResource(R.string.module_not_active)) },
        text = { Text(stringResource(R.string.module_not_active_message)) },
        confirmButton = {}, // No confirm button
        dismissButton = null // No dismiss button
    )
} 