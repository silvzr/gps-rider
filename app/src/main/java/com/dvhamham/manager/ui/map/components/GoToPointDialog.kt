package com.dvhamham.manager.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dvhamham.manager.ui.map.MapViewModel

@Composable
fun GoToPointDialog(
    mapViewModel: MapViewModel,
    onDismissRequest: () -> Unit,
    onGoToPoint: (latitude: Double, longitude: Double) -> Unit
) {
    // Access the UI state through StateFlow
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val goToPointState = uiState.goToPointState
    
    val coordinatesInput = goToPointState.value
    val coordinatesError = goToPointState.errorMessage
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Auto-focus when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .clickable { 
                mapViewModel.clearGoToPointInputs()
                onDismissRequest() 
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(enabled = false) { } // Prevent click propagation
        ) {
            OutlinedTextField(
                value = coordinatesInput,
                onValueChange = { mapViewModel.updateGoToPointField("coordinates", it) },
                label = { Text("Enter coordinates (lat, lng)") },
                placeholder = { Text("e.g., 40.7128, -74.0060") },
                isError = coordinatesError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onKeyEvent { keyEvent ->
                        when (keyEvent.key) {
                            Key.Enter -> {
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    mapViewModel.validateAndGo { latitude, longitude ->
                                        onGoToPoint(latitude, longitude)
                                    }
                                    true
                                } else false
                            }
                            Key.Escape -> {
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    mapViewModel.clearGoToPointInputs()
                                    onDismissRequest()
                                    true
                                } else false
                            }
                            else -> false
                        }
                    },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            if (coordinatesError != null) {
                Text(
                    text = coordinatesError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Press Enter to go to coordinates • Press Esc to cancel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
