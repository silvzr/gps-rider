package com.dvhamham.manager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ModuleNotActiveDialog() {
    AlertDialog(
        onDismissRequest = {}, // Non-cancelable
        title = { Text("Module not active") },
        text = { Text("The GPS Rider module is not active in LSPosed/Xposed. Please enable the module and restart the app.") },
        confirmButton = {}, // No confirm button
        dismissButton = null // No dismiss button
    )
} 